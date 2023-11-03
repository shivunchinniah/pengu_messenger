package UI;

import UI.Custom.ActiveChatsSelection;
import UI.Custom.ContactSelection;
import UI.Custom.MessageViewBox;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.util.Calendar;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import pengu.messenger.PenguMessengerClient;
import pengu.messenger.PenguRESTClient;
import UI.Custom.RoundedCornerBorder;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * This class creates a Main Screen Frame
 * @author Shivun Chinniah
 */
public class MainScreen extends javax.swing.JFrame {

    private static final String TEXT_SUBMIT = "text-submit";
    private final PenguMessengerClient pmc;
    private String onlineContactToAdd = "";
    private String chatTarget = "";
    private ContactSelection contactSelection;
    private final ActiveChatsSelection activeChatSelection;
    private boolean online = true;
    private boolean loggedin = true;

    /**
     * Parameterized constructor method.
     * 
     * @param pmc The PenguMessengerClient to use.
     */
    public MainScreen(PenguMessengerClient pmc) {
        this.pmc = pmc;
        initComponents();
        this.setTitle(pmc.getUsername().toLowerCase() + " - Pengu Messenger");

        jRadioButton1.requestFocusInWindow();

        //getContentPane().add("contactscard", Contacts);
        //getContentPane().add("settingscard", SettingsPanel);
        pnlMessageInput.setVisible(false);
        this.lblActiveChatUsername.setText("");
        this.lblActiveChatFriendlyName.setText("");
        //this.pnlActivity.removeAll();
        this.pnlActivity.setLayout(new BorderLayout());

        activeChatSelection = new ActiveChatsSelection(pmc) {
            @Override
            public void selectedAction() {
                contactSelected();
            }

            @Override
            public void goToContacts() {
                contactsShow();
            }

            @Override
            public void respondToDeleteChat() {
                chatDeleted(getDeleteChatTarget());
            }

        };

        pnlActiveChatsHolder.setLayout(new BorderLayout());
        pnlActiveChatsHolder.add(activeChatSelection);

        new Thread() {

            @Override
            public void run() {
                //long x = 0;
                while (loggedin) {
                    try {
                        //System.out.println(x);
                        //x++;
                        cycle();
                        Thread.sleep(500);

                    } catch (InterruptedException ex) {
                        System.out.println("Thread error: " + ex);
                    }
                }
            }

        }.start();

    }

    // this method is called repeatedly
    private void cycle() {
        try {
            if (online) {
                lblClientOnlineStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UI/resources/green_light.png")));
            } else {
                lblClientOnlineStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UI/resources/red_light.png")));
            }

            ArrayList<Integer> changeChatIDs = pmc.updateCycle();

            online = true;
            if (changeChatIDs.contains(activeChatSelection.getSelectedChatID())) {
                //addMessageBox(selectedChatUsername, selectedChatID);
                activeChatSelection.selectedAction();

                //System.out.println("change");
            } else {
                activeChatSelection.reloadPreserveScroll();
            }

        } catch (PenguRESTClient.UnauthorisedException ex) {
            DateFormat df = new SimpleDateFormat("HH:mm dd/MM");
            System.out.println("...");
            LogoutAction("You were automatically logged out @ " + df.format(new Date()), new Color(53, 6, 48));

        } catch (PenguMessengerClient.ServerProblemException ex) {
            online = false;
            //System.out.println(ex.getDescription());
        } catch (PenguRESTClient.ForbiddenException ex) {
            System.out.println(ex + " @ cycle");
        }
    }

    /**
     * Sends Message to selectedUserName or selectedChatID
     *
     * @param message The message content.
     */
    private void sendMessage(String message) {
        pmc.outboxMessage(activeChatSelection.getSelectedChatID(), message, activeChatSelection.getSelectedUsername());
        //addMessageBox(selectedChatUsername, selectedChatID);

        activeChatSelection.reload();
        activeChatSelection.selectedAction();

        this.revalidate();
        txtMessageInput.setText("");

    }

    /**
     * Validates that the content being sent is of good enough quality, and if
     * it is, the sendMessage method is called.
     */
    private void validateSend() {
        String messageContent = txtMessageInput.getText();
        //System.out.println(messageContent);

        messageContent = messageContent.trim();//remove white spaces

        if (messageContent.length() > 10000) {
            JOptionPane.showMessageDialog(this, "Maximum charater limit exceeded! (10 000 characters)", "> 10 000 Characters in message", JOptionPane.WARNING_MESSAGE);
            txtMessageInput.setText(messageContent.substring(0, 10000));
        } else {
            if (messageContent.length() != 0) {
                sendMessage(messageContent);
            } else {
                txtMessageInput.setText("");
            }

        }

    }

    private void LogoutAction(String displayOnLoginScreen, Color colorOfText) {
        loggedin = false;
        new LoginScreen(displayOnLoginScreen, colorOfText).setVisible(true);
        this.dispose();

    }

    private void chatDeleted(int chatID) {
        if (activeChatSelection.getSelectedChatID() == chatID) {
            activeChatSelection.resetSelectionVars();
            pnlMessageInput.setVisible(false);
            pnlActivity.removeAll();
            jLabel1 = new javax.swing.JLabel();

            jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N

            jLabel1.setForeground(new java.awt.Color(204, 204, 204));

            jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

            jLabel1.setText("No Chat Selected");
            pnlActivity.add(jLabel1);
            lblActiveChatUsername.setText("");
            lblActiveChatFriendlyName.setText("");
            this.revalidate();
        } else {
            //do nothing
        }
    }

    private void contactSelected() {
        pnlActivity.removeAll();
        pnlActivity.setLayout(new BorderLayout());
        pnlActivity.add(new MessageViewBox(activeChatSelection.getSelectedChatID(), pnlActivity.getHeight(), pmc));
        pnlMessageInput.setVisible(true);
        lblActiveChatUsername.setText(activeChatSelection.getSelectedUsername());
        if (pmc.getDetailsForContact(activeChatSelection.getSelectedUsername()).getDisplayName() != null) {
            lblActiveChatFriendlyName.setText(pmc.getDetailsForContact(activeChatSelection.getSelectedUsername()).getDisplayName());
            lblActiveChatFriendlyName.setForeground(pmc.getDetailsForContact(activeChatSelection.getSelectedUsername()).getContactGroupColor());
        } else {
            lblActiveChatFriendlyName.setText("");

        }

        //lblActiveChatFriendlyName.setText(pmc.getDetailsForContact(activeChatSelection.getSelectedUsername())[0].getDisplayName());
        //lblActiveChatFriendlyName.setForeground(pmc.getDetailsForContact(activeChatSelection.getSelectedUsername())[0].getContactGroupColor());
        //lblActiveChatUsername.setForeground(pmc.getDetailsForContact(activeChatSelection.getSelectedUsername())[0].getContactGroupColor());
        this.revalidate();
    }

    //Contacts page methods
    private void loadSelectedContact(String contactUsername) {
        PenguMessengerClient.ContactItem temp = pmc.getDetailsForContact(contactUsername);
        chatTarget = contactUsername;

        if (temp == null) {//online contact, contact does not exist in database
            //System.out.println("online contact selected");
            CardLayout cl = (CardLayout) pnlContactsSwitch.getLayout();
            cl.show(pnlContactsSwitch, "online");
            pnlContactType.setBackground(new Color(48, 147, 121));
            lblContactType.setText("Online Contact");
            lblOnlineContactUsername.setText(contactUsername);
            onlineContactToAdd = contactUsername;

        } else {

            CardLayout cl = (CardLayout) pnlContactsSwitch.getLayout();
            cl.show(pnlContactsSwitch, "normal");
            
            
            lblContactUsername.setText(temp.getUsername());
            
            
            if (temp.getDisplayName() == null) {
                lblContactFriendlyName.setText("(Friendly Name)");
            } else {
                lblContactFriendlyName.setText(temp.getDisplayName());
            }

            txtAdditionalInformation.setText(temp.getAdditionalInformation());
            pnlContactType.setBackground(temp.getContactGroupColor());
            lblContactType.setText(temp.getContactGroup() + " Contact");
            lblPublicKeyHash.setText(PenguMessengerClient.getSHA1Hash(temp.getPublicKey()));

            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());

            long now = cal.getTimeInMillis();

            cal.setTime(temp.getPublicKeyExpiryDate());

            long expires = cal.getTimeInMillis();

            int hourDiff = (int) ((expires - now) / (long) 3600000);

            int days = (int) hourDiff / (int) 24;
            int hours = hourDiff % 24;
            if (hourDiff < 0) {
                lblPublicKeyExpiryDate.setText("Expired " + -days + " days, " + -hours + " hours ago");
            } else {
                lblPublicKeyExpiryDate.setText(days + " days, " + hours + " hours");
            }

        }

    }

    private void editChatTargetFriendlyName() {
        PenguMessengerClient.ContactItem temp = pmc.getDetailsForContact(chatTarget);

        String input = JOptionPane.showInputDialog(this, "Enter a Friendly Name", temp.getDisplayName());
        if (input == null) {//cancelbutton

        } else if (input.trim().length() == 0) {// no data
            pmc.editContactFriendlyName(chatTarget, null);
            lblContactFriendlyName.setText("(Friendly Name)");
            contactSelection.reloadContacts(chatTarget);
            activeChatSelection.reload();

        } else {

            pmc.editContactFriendlyName(chatTarget, input);
            lblContactFriendlyName.setText(input);
            contactSelection.reloadContacts(chatTarget);
            activeChatSelection.reload();
        }

        if (activeChatSelection.getSelectedUsername().equalsIgnoreCase(chatTarget)) {
            lblActiveChatFriendlyName.setText(pmc.getDetailsForContact(activeChatSelection.getSelectedUsername()).getDisplayName());
            lblActiveChatFriendlyName.setForeground(pmc.getDetailsForContact(activeChatSelection.getSelectedUsername()).getContactGroupColor());
        }

    }

    private void editChatTargetContactGroup() {
        PenguMessengerClient.ContactType[] temp = pmc.getContactTypes();
        PenguMessengerClient.ContactItem current = pmc.getDetailsForContact(chatTarget);
        
        
        PenguMessengerClient.ContactType currentContactType = new PenguMessengerClient.ContactType(-1, current.getContactGroup(), current.getContactGroupColor());
        
        
        
        PenguMessengerClient.ContactType selection = (PenguMessengerClient.ContactType) JOptionPane.showInputDialog(null, "Choose a contact group",
                "Choose a contact group for " + chatTarget, JOptionPane.QUESTION_MESSAGE, null, // Use
                // default
                // icon
                temp, // Array of choices
                currentContactType);

        if (selection == null || selection.equals(currentContactType)) {//cancelbutton or no change

        } else {

            pmc.setContactsGroup(chatTarget, selection.getContactTypeID());
            
            pnlContactType.setBackground(selection.getContactGroupColour());
            lblContactType.setText(selection.getGroupName()+ " Contact");
            
            contactSelection.reloadContacts(chatTarget);
            activeChatSelection.reload();
        }
        
        if (activeChatSelection.getSelectedUsername().equalsIgnoreCase(chatTarget)) {
            
            lblActiveChatFriendlyName.setForeground(pmc.getDetailsForContact(activeChatSelection.getSelectedUsername()).getContactGroupColor());
        }

        

    }

    private void reloadContactsSelection() {

        pnlContactsHolder.removeAll();
        contactSelection = new ContactSelection(pmc) {
            @Override
            public void showProperties() {
                loadSelectedContact(getSelected()); //To change body of generated methods, choose Tools | Templates.
            }

        };

        pnlContactsHolder.add(contactSelection);
        pnlContactsHolder.revalidate();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDesktopPane1 = new javax.swing.JDesktopPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        MainView = new javax.swing.JPanel();
        pnlMessageInput = new javax.swing.JPanel();
        pnlRoundMessageHolder = new javax.swing.JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque() && getBorder() instanceof RoundedCornerBorder) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setPaint(getBackground());
                    g2.fill(((RoundedCornerBorder) getBorder()).getBorderShape(
                        0, 0, getWidth() - 1, getHeight() - 1));
                g2.dispose();
            }
            super.paintComponent(g);
        }
        @Override
        public void updateUI() {
            super.updateUI();
            setOpaque(false);
            setBorder(new RoundedCornerBorder(new Color(204,204,204), 20));
        }
    };
    jScrollPane2 = new UI.Custom.ModernScrollPane(txtMessageInput, Color.gray){
        @Override
        protected void paintComponent(Graphics g) {
            if (!isOpaque() && getBorder() instanceof RoundedCornerBorder) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(getBackground());
                g2.fill(((RoundedCornerBorder) getBorder()).getBorderShape(
                    0, 0, getWidth() - 1, getHeight() - 1));
            g2.dispose();
        }
        super.paintComponent(g);
    }
    @Override
    public void updateUI() {
        super.updateUI();
        setOpaque(false);
        setBorder(new RoundedCornerBorder());
    }
    };
    txtMessageInput = new javax.swing.JTextArea()
    ;
    btnSend = new javax.swing.JLabel();
    pnlActivity = new javax.swing.JPanel();
    jRadioButton1 = new javax.swing.JRadioButton();
    jLabel1 = new javax.swing.JLabel();
    pnlBar = new javax.swing.JPanel();
    lblClientOnlineStatus = new javax.swing.JLabel();
    lblPenguMessengerHeader = new javax.swing.JLabel();
    jLabel6 = new javax.swing.JLabel();
    jLabel7 = new javax.swing.JLabel();
    pnlInfoHeader = new javax.swing.JPanel();
    btnContacts = new javax.swing.JLabel();
    btnSettings = new javax.swing.JLabel();
    lblActiveChatUsername = new javax.swing.JLabel();
    lblActiveChatFriendlyName = new javax.swing.JLabel();
    pnlActiveChatsHolder = new javax.swing.JPanel();
    SettingsPanel = new javax.swing.JPanel();
    pnlSettingContent = new javax.swing.JPanel();
    lblSettingName = new javax.swing.JLabel();
    pnlGreenHead = new javax.swing.JPanel();
    lblSettingDescription = new javax.swing.JLabel();
    pnlSettingArea = new javax.swing.JPanel();
    pnlSelectSetting = new javax.swing.JPanel();
    lblSettingsAnimation = new javax.swing.JLabel();
    pnlHelp = new javax.swing.JPanel();
    lblForHelpHeader = new javax.swing.JLabel();
    lblEmail = new javax.swing.JLabel();
    lblEmailAddress = new javax.swing.JLabel();
    lblWebHeader = new javax.swing.JLabel();
    lblLinkHelp = new javax.swing.JLabel();
    jSeparator1 = new javax.swing.JSeparator();
    jSeparator3 = new javax.swing.JSeparator();
    pnlAbout = new javax.swing.JPanel();
    lblVersionHeader = new javax.swing.JLabel();
    lblVersion = new javax.swing.JLabel();
    lblDevelopedBy = new javax.swing.JLabel();
    lblDevelopedByHeader = new javax.swing.JLabel();
    lblReleaseDateHeader = new javax.swing.JLabel();
    lblReleaseDate = new javax.swing.JLabel();
    lblAboutHeader = new javax.swing.JLabel();
    lblAboutLink = new javax.swing.JLabel();
    pnlLogout = new javax.swing.JPanel();
    btnLogoutConfirm = new javax.swing.JButton();
    jLabel2 = new javax.swing.JLabel();
    jLabel3 = new javax.swing.JLabel();
    pnlSettingsButtonsHolder = new javax.swing.JPanel();
    btnBack = new javax.swing.JLabel();
    lblSettingsHeader = new javax.swing.JLabel();
    jSeparator2 = new javax.swing.JSeparator();
    btnHelp = new javax.swing.JButton();
    btnAbout = new javax.swing.JButton();
    btnLogout = new javax.swing.JButton();
    Contacts = new javax.swing.JPanel();
    pnlSearchHolder = new javax.swing.JPanel();
    btnBackContacts = new javax.swing.JLabel();
    lblContactsHeader = new javax.swing.JLabel();
    pnlContactsHolder = new javax.swing.JPanel();
    pnlContactsHolderFooter = new javax.swing.JPanel();
    pnlContactType = new javax.swing.JPanel();
    lblContactType = new javax.swing.JLabel();
    pnlContactsSwitch = new javax.swing.JPanel();
    pnlNoContactSelected = new javax.swing.JPanel();
    jLabel4 = new javax.swing.JLabel();
    jLabel5 = new javax.swing.JLabel();
    pnlContactView = new javax.swing.JPanel();
    lblContactUsername = new javax.swing.JLabel();
    lblPublicKeyHeader = new javax.swing.JLabel();
    lblContactFriendlyName = new javax.swing.JLabel();
    pnlAddtionalInfoHolder = new javax.swing.JPanel(){
        @Override
        protected void paintComponent(Graphics g) {
            if (!isOpaque() && getBorder() instanceof RoundedCornerBorder) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(getBackground());
                g2.fill(((RoundedCornerBorder) getBorder()).getBorderShape(
                    0, 0, getWidth() - 1, getHeight() - 1));
            g2.dispose();
        }
        super.paintComponent(g);
    }
    @Override
    public void updateUI() {
        super.updateUI();
        setOpaque(false);
        setBorder(new RoundedCornerBorder(Color.WHITE, 20));
    }
    };
    jScrollPane1 = new UI.Custom.ModernScrollPane(txtAdditionalInformation, Color.gray);
    txtAdditionalInformation = new javax.swing.JTextArea()
    ;
    lblAdditionalInformationHeader = new javax.swing.JLabel();
    lblPublicKeyHash = new javax.swing.JLabel();
    lblPublicKeyExpiryHeader = new javax.swing.JLabel();
    lblPublicKeyExpiryDate = new javax.swing.JLabel();
    btnCreateChatWithUser = new javax.swing.JLabel();
    btnEditFriendlyName = new javax.swing.JLabel();
    jLabel13 = new javax.swing.JLabel();
    btnEditContactGroup = new javax.swing.JLabel();
    pnlOnlineContactView = new javax.swing.JPanel();
    lblOnlineContactUsername = new javax.swing.JLabel();
    btnNewChat = new javax.swing.JButton();
    btnAddContact = new javax.swing.JButton();

    javax.swing.GroupLayout jDesktopPane1Layout = new javax.swing.GroupLayout(jDesktopPane1);
    jDesktopPane1.setLayout(jDesktopPane1Layout);
    jDesktopPane1Layout.setHorizontalGroup(
        jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 100, Short.MAX_VALUE)
    );
    jDesktopPane1Layout.setVerticalGroup(
        jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 100, Short.MAX_VALUE)
    );

    jList1.setModel(new javax.swing.AbstractListModel<String>() {
        String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
        public int getSize() { return strings.length; }
        public String getElementAt(int i) { return strings[i]; }
    });
    jScrollPane3.setViewportView(jList1);

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("Pengu Messenger");
    setIconImage((new javax.swing.ImageIcon(getClass().getResource("/UI/resources/Pengu Messenger.png")).getImage()));
    setSize(new java.awt.Dimension(735, 414));
    getContentPane().setLayout(new java.awt.CardLayout());

    MainView.setName("Main"); // NOI18N

    pnlMessageInput.setBackground(new java.awt.Color(204, 204, 204));

    pnlRoundMessageHolder.setBackground(new java.awt.Color(255, 255, 255));

    txtMessageInput.setColumns(10);
    txtMessageInput.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
    txtMessageInput.setForeground(new java.awt.Color(51, 51, 51));
    txtMessageInput.setLineWrap(true);
    txtMessageInput.setRows(1);
    txtMessageInput.setWrapStyleWord(true);
    txtMessageInput.setMargin(new java.awt.Insets(0, 0, 0, 8));
    jScrollPane2.setViewportView(txtMessageInput);
    InputMap input = txtMessageInput.getInputMap();
    //KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
    KeyStroke shiftEnter = KeyStroke.getKeyStroke("shift ENTER");
    input.put(shiftEnter, TEXT_SUBMIT);  // input.get(enter)) = "insert-break"
    //input.put(enter, TEXT_SUBMIT);

    ActionMap actions = txtMessageInput.getActionMap();
    actions.put(TEXT_SUBMIT, new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            validateSend();

        }
    });

    javax.swing.GroupLayout pnlRoundMessageHolderLayout = new javax.swing.GroupLayout(pnlRoundMessageHolder);
    pnlRoundMessageHolder.setLayout(pnlRoundMessageHolderLayout);
    pnlRoundMessageHolderLayout.setHorizontalGroup(
        pnlRoundMessageHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlRoundMessageHolderLayout.createSequentialGroup()
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
            .addGap(0, 0, 0))
    );
    pnlRoundMessageHolderLayout.setVerticalGroup(
        pnlRoundMessageHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
    );

    btnSend.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    btnSend.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UI/resources/Sendbutton.png"))); // NOI18N
    btnSend.setToolTipText("Press [SHIFT] + [ENTER] to send message");
    btnSend.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    btnSend.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            btnSendMouseClicked(evt);
        }
    });

    javax.swing.GroupLayout pnlMessageInputLayout = new javax.swing.GroupLayout(pnlMessageInput);
    pnlMessageInput.setLayout(pnlMessageInputLayout);
    pnlMessageInputLayout.setHorizontalGroup(
        pnlMessageInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlMessageInputLayout.createSequentialGroup()
            .addGap(26, 26, 26)
            .addComponent(pnlRoundMessageHolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(20, 20, 20)
            .addComponent(btnSend)
            .addGap(22, 22, 22))
    );
    pnlMessageInputLayout.setVerticalGroup(
        pnlMessageInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlMessageInputLayout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(pnlMessageInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(btnSend, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addGroup(pnlMessageInputLayout.createSequentialGroup()
                    .addComponent(pnlRoundMessageHolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(5, 5, 5)))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    pnlActivity.setBackground(new java.awt.Color(102, 102, 102));

    jRadioButton1.setText("jRadioButton1");
    jRadioButton1.setFocusCycleRoot(true);

    jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
    jLabel1.setForeground(new java.awt.Color(204, 204, 204));
    jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel1.setText("No Chat Selected");

    javax.swing.GroupLayout pnlActivityLayout = new javax.swing.GroupLayout(pnlActivity);
    pnlActivity.setLayout(pnlActivityLayout);
    pnlActivityLayout.setHorizontalGroup(
        pnlActivityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlActivityLayout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(jRadioButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlActivityLayout.createSequentialGroup()
            .addContainerGap(261, Short.MAX_VALUE)
            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(228, Short.MAX_VALUE))
    );
    pnlActivityLayout.setVerticalGroup(
        pnlActivityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlActivityLayout.createSequentialGroup()
            .addGap(0, 0, 0)
            .addComponent(jRadioButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, 0)
            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 478, Short.MAX_VALUE))
    );

    pnlBar.setBackground(new java.awt.Color(69, 162, 158));
    pnlBar.setAlignmentX(0.0F);
    pnlBar.setMaximumSize(new java.awt.Dimension(253, 32767));

    lblClientOnlineStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UI/resources/green_light.png"))); // NOI18N
    lblClientOnlineStatus.setToolTipText("Red - Offline, Green - Online");

    lblPenguMessengerHeader.setFont(new java.awt.Font("Bungee Hairline", 0, 24)); // NOI18N
    lblPenguMessengerHeader.setForeground(new java.awt.Color(255, 255, 255));
    lblPenguMessengerHeader.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UI/resources/Pengu Messenger Header.png"))); // NOI18N

    jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UI/resources/pengu messenger logo small.png"))); // NOI18N

    jLabel7.setForeground(new java.awt.Color(51, 51, 51));
    jLabel7.setText("End-End Encryption");

    javax.swing.GroupLayout pnlBarLayout = new javax.swing.GroupLayout(pnlBar);
    pnlBar.setLayout(pnlBarLayout);
    pnlBarLayout.setHorizontalGroup(
        pnlBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlBarLayout.createSequentialGroup()
            .addGap(14, 14, 14)
            .addComponent(jLabel6)
            .addGroup(pnlBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlBarLayout.createSequentialGroup()
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(pnlBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblPenguMessengerHeader))
                    .addContainerGap(31, Short.MAX_VALUE))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlBarLayout.createSequentialGroup()
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblClientOnlineStatus)
                    .addContainerGap())))
    );
    pnlBarLayout.setVerticalGroup(
        pnlBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlBarLayout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(pnlBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlBarLayout.createSequentialGroup()
                    .addComponent(jLabel7)
                    .addGap(5, 5, 5)
                    .addComponent(lblPenguMessengerHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addGroup(pnlBarLayout.createSequentialGroup()
            .addComponent(lblClientOnlineStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE))
    );

    pnlInfoHeader.setBackground(new java.awt.Color(181, 181, 181));
    pnlInfoHeader.setAlignmentX(0.0F);

    btnContacts.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UI/resources/contactsmedium.png"))); // NOI18N
    btnContacts.setToolTipText("Contacts");
    btnContacts.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    btnContacts.setPreferredSize(new java.awt.Dimension(25, 25));
    btnContacts.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            btnContactsMouseClicked(evt);
        }
    });

    btnSettings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UI/resources/settings.png"))); // NOI18N
    btnSettings.setToolTipText("Settings");
    btnSettings.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    btnSettings.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            btnSettingsMouseClicked(evt);
        }
    });

    lblActiveChatUsername.setFont(new java.awt.Font("Bungee", 0, 28)); // NOI18N
    lblActiveChatUsername.setForeground(new java.awt.Color(51, 51, 51));
    lblActiveChatUsername.setText("pengu101");

    lblActiveChatFriendlyName.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
    lblActiveChatFriendlyName.setForeground(new java.awt.Color(255, 255, 255));
    lblActiveChatFriendlyName.setText("Friendlyname");

    javax.swing.GroupLayout pnlInfoHeaderLayout = new javax.swing.GroupLayout(pnlInfoHeader);
    pnlInfoHeader.setLayout(pnlInfoHeaderLayout);
    pnlInfoHeaderLayout.setHorizontalGroup(
        pnlInfoHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlInfoHeaderLayout.createSequentialGroup()
            .addGap(37, 37, 37)
            .addGroup(pnlInfoHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(lblActiveChatFriendlyName, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                .addComponent(lblActiveChatUsername, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 235, Short.MAX_VALUE)
            .addComponent(btnContacts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(btnSettings)
            .addGap(14, 14, 14))
    );
    pnlInfoHeaderLayout.setVerticalGroup(
        pnlInfoHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(btnContacts, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(btnSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(pnlInfoHeaderLayout.createSequentialGroup()
            .addGap(19, 19, 19)
            .addComponent(lblActiveChatUsername)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(lblActiveChatFriendlyName, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(20, Short.MAX_VALUE))
    );

    pnlActiveChatsHolder.setBackground(new java.awt.Color(254, 254, 254));

    javax.swing.GroupLayout pnlActiveChatsHolderLayout = new javax.swing.GroupLayout(pnlActiveChatsHolder);
    pnlActiveChatsHolder.setLayout(pnlActiveChatsHolderLayout);
    pnlActiveChatsHolderLayout.setHorizontalGroup(
        pnlActiveChatsHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 0, Short.MAX_VALUE)
    );
    pnlActiveChatsHolderLayout.setVerticalGroup(
        pnlActiveChatsHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 0, Short.MAX_VALUE)
    );

    javax.swing.GroupLayout MainViewLayout = new javax.swing.GroupLayout(MainView);
    MainView.setLayout(MainViewLayout);
    MainViewLayout.setHorizontalGroup(
        MainViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(MainViewLayout.createSequentialGroup()
            .addGroup(MainViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(pnlBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlActiveChatsHolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(MainViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pnlInfoHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlMessageInput, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlActivity, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
    );
    MainViewLayout.setVerticalGroup(
        MainViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(MainViewLayout.createSequentialGroup()
            .addGroup(MainViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(pnlInfoHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(0, 0, 0)
            .addGroup(MainViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(MainViewLayout.createSequentialGroup()
                    .addComponent(pnlActivity, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(1, 1, 1)
                    .addComponent(pnlMessageInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(pnlActiveChatsHolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
    );

    getContentPane().add(MainView, "maincard");

    SettingsPanel.setBackground(new java.awt.Color(255, 255, 255));

    pnlSettingContent.setBackground(new java.awt.Color(255, 255, 255));

    lblSettingName.setFont(new java.awt.Font("Arial", 1, 36)); // NOI18N
    lblSettingName.setForeground(new java.awt.Color(84, 84, 84));
    lblSettingName.setText("Setting Name");

    pnlGreenHead.setBackground(new java.awt.Color(0, 178, 80));

    javax.swing.GroupLayout pnlGreenHeadLayout = new javax.swing.GroupLayout(pnlGreenHead);
    pnlGreenHead.setLayout(pnlGreenHeadLayout);
    pnlGreenHeadLayout.setHorizontalGroup(
        pnlGreenHeadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 0, Short.MAX_VALUE)
    );
    pnlGreenHeadLayout.setVerticalGroup(
        pnlGreenHeadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 61, Short.MAX_VALUE)
    );

    lblSettingDescription.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
    lblSettingDescription.setForeground(new java.awt.Color(102, 102, 102));
    lblSettingDescription.setText("Description of setting");

    pnlSettingArea.setBackground(new java.awt.Color(217, 217, 217));
    pnlSettingArea.setLayout(new java.awt.CardLayout());

    pnlSelectSetting.setBackground(new java.awt.Color(255, 255, 255));

    lblSettingsAnimation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UI/resources/settings slide.gif"))); // NOI18N

    javax.swing.GroupLayout pnlSelectSettingLayout = new javax.swing.GroupLayout(pnlSelectSetting);
    pnlSelectSetting.setLayout(pnlSelectSettingLayout);
    pnlSelectSettingLayout.setHorizontalGroup(
        pnlSelectSettingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSelectSettingLayout.createSequentialGroup()
            .addContainerGap(171, Short.MAX_VALUE)
            .addComponent(lblSettingsAnimation)
            .addGap(133, 133, 133))
    );
    pnlSelectSettingLayout.setVerticalGroup(
        pnlSelectSettingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlSelectSettingLayout.createSequentialGroup()
            .addGap(147, 147, 147)
            .addComponent(lblSettingsAnimation, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(242, Short.MAX_VALUE))
    );

    pnlSettingArea.add(pnlSelectSetting, "SelectSetting");

    pnlHelp.setBackground(new java.awt.Color(255, 255, 255));

    lblForHelpHeader.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
    lblForHelpHeader.setForeground(new java.awt.Color(51, 51, 51));
    lblForHelpHeader.setText("For Help or To Report Bugs:");

    lblEmail.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
    lblEmail.setText("Email Shivun Chinniah (Developer):");

    lblEmailAddress.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
    lblEmailAddress.setForeground(new java.awt.Color(0, 102, 102));
    lblEmailAddress.setText("shivun@diorb.com");

    lblWebHeader.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
    lblWebHeader.setText("View Pengu Messenger's Online Help Document:");

    lblLinkHelp.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
    lblLinkHelp.setForeground(new java.awt.Color(255, 102, 102));
    lblLinkHelp.setText("https://www.diorb.com/pengumessenger/help/");
    lblLinkHelp.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    lblLinkHelp.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            lblLinkHelpMouseClicked(evt);
        }
    });

    javax.swing.GroupLayout pnlHelpLayout = new javax.swing.GroupLayout(pnlHelp);
    pnlHelp.setLayout(pnlHelpLayout);
    pnlHelpLayout.setHorizontalGroup(
        pnlHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlHelpLayout.createSequentialGroup()
            .addGap(44, 44, 44)
            .addGroup(pnlHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 427, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(pnlHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblEmailAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblForHelpHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblWebHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLinkHelp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 427, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(83, Short.MAX_VALUE))
    );
    pnlHelpLayout.setVerticalGroup(
        pnlHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlHelpLayout.createSequentialGroup()
            .addGap(67, 67, 67)
            .addComponent(lblForHelpHeader)
            .addGap(28, 28, 28)
            .addComponent(lblEmail)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(lblEmailAddress)
            .addGap(20, 20, 20)
            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(30, 30, 30)
            .addComponent(lblWebHeader)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(lblLinkHelp)
            .addGap(20, 20, 20)
            .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(220, Short.MAX_VALUE))
    );

    pnlSettingArea.add(pnlHelp, "Help");

    pnlAbout.setBackground(new java.awt.Color(255, 255, 255));

    lblVersionHeader.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
    lblVersionHeader.setText("Version:");

    lblVersion.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
    lblVersion.setForeground(new java.awt.Color(0, 102, 102));
    lblVersion.setText("1.0");

    lblDevelopedBy.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
    lblDevelopedBy.setForeground(new java.awt.Color(89, 102, 0));
    lblDevelopedBy.setText("Shivun Chinniah");

    lblDevelopedByHeader.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
    lblDevelopedByHeader.setText("Developed by:");

    lblReleaseDateHeader.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
    lblReleaseDateHeader.setText("Release Date:");

    lblReleaseDate.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
    lblReleaseDate.setForeground(new java.awt.Color(102, 18, 0));
    lblReleaseDate.setText("22/08/2018");

    lblAboutHeader.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
    lblAboutHeader.setText("About Pengu Messenger (Read more):");

    lblAboutLink.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
    lblAboutLink.setForeground(new java.awt.Color(102, 0, 102));
    lblAboutLink.setText("https://www.diorb.com/pengumessenger/about");
    lblAboutLink.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    lblAboutLink.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            lblAboutLinkMouseClicked(evt);
        }
    });

    javax.swing.GroupLayout pnlAboutLayout = new javax.swing.GroupLayout(pnlAbout);
    pnlAbout.setLayout(pnlAboutLayout);
    pnlAboutLayout.setHorizontalGroup(
        pnlAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlAboutLayout.createSequentialGroup()
            .addGap(49, 49, 49)
            .addGroup(pnlAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lblAboutHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblReleaseDate, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblReleaseDateHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblDevelopedBy, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblDevelopedByHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblVersionHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblAboutLink, javax.swing.GroupLayout.PREFERRED_SIZE, 387, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(118, Short.MAX_VALUE))
    );
    pnlAboutLayout.setVerticalGroup(
        pnlAboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlAboutLayout.createSequentialGroup()
            .addGap(58, 58, 58)
            .addComponent(lblVersionHeader)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(lblVersion)
            .addGap(25, 25, 25)
            .addComponent(lblDevelopedByHeader)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(lblDevelopedBy)
            .addGap(25, 25, 25)
            .addComponent(lblReleaseDateHeader)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(lblReleaseDate)
            .addGap(25, 25, 25)
            .addComponent(lblAboutHeader)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(lblAboutLink)
            .addContainerGap(201, Short.MAX_VALUE))
    );

    pnlSettingArea.add(pnlAbout, "About");

    pnlLogout.setBackground(new java.awt.Color(255, 255, 255));

    btnLogoutConfirm.setBackground(new java.awt.Color(181, 181, 181));
    btnLogoutConfirm.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
    btnLogoutConfirm.setForeground(new java.awt.Color(68, 68, 68));
    btnLogoutConfirm.setText("Confirm Log-out");
    btnLogoutConfirm.setBorder(null);
    btnLogoutConfirm.setFocusPainted(false);
    btnLogoutConfirm.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnLogoutConfirmActionPerformed(evt);
        }
    });

    jLabel2.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
    jLabel2.setForeground(new java.awt.Color(51, 51, 51));
    jLabel2.setText("Note:");

    jLabel3.setText("You will be redirected to the Log-in screen, are you sure you want to log-out?");

    javax.swing.GroupLayout pnlLogoutLayout = new javax.swing.GroupLayout(pnlLogout);
    pnlLogout.setLayout(pnlLogoutLayout);
    pnlLogoutLayout.setHorizontalGroup(
        pnlLogoutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlLogoutLayout.createSequentialGroup()
            .addGroup(pnlLogoutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jLabel3)
                .addGroup(pnlLogoutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlLogoutLayout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jLabel2))
                    .addGroup(pnlLogoutLayout.createSequentialGroup()
                        .addGap(74, 74, 74)
                        .addComponent(btnLogoutConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, 372, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addContainerGap(108, Short.MAX_VALUE))
    );
    pnlLogoutLayout.setVerticalGroup(
        pnlLogoutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlLogoutLayout.createSequentialGroup()
            .addGap(92, 92, 92)
            .addComponent(jLabel2)
            .addGap(18, 18, 18)
            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(27, 27, 27)
            .addComponent(btnLogoutConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(261, Short.MAX_VALUE))
    );

    pnlSettingArea.add(pnlLogout, "Logout");

    javax.swing.GroupLayout pnlSettingContentLayout = new javax.swing.GroupLayout(pnlSettingContent);
    pnlSettingContent.setLayout(pnlSettingContentLayout);
    pnlSettingContentLayout.setHorizontalGroup(
        pnlSettingContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(pnlGreenHead, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(pnlSettingArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(pnlSettingContentLayout.createSequentialGroup()
            .addGap(26, 26, 26)
            .addGroup(pnlSettingContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lblSettingName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlSettingContentLayout.createSequentialGroup()
                    .addComponent(lblSettingDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 478, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addContainerGap())
    );
    pnlSettingContentLayout.setVerticalGroup(
        pnlSettingContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlSettingContentLayout.createSequentialGroup()
            .addComponent(pnlGreenHead, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(19, 19, 19)
            .addComponent(lblSettingName)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(lblSettingDescription)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
            .addComponent(pnlSettingArea, javax.swing.GroupLayout.PREFERRED_SIZE, 534, javax.swing.GroupLayout.PREFERRED_SIZE))
    );

    pnlSettingsButtonsHolder.setBackground(new java.awt.Color(217, 217, 217));
    pnlSettingsButtonsHolder.setForeground(new java.awt.Color(148, 148, 148));

    btnBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UI/resources/backArrow.png"))); // NOI18N
    btnBack.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    btnBack.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            btnBackMouseClicked(evt);
        }
    });

    lblSettingsHeader.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UI/resources/settings header.png"))); // NOI18N

    jSeparator2.setBackground(new java.awt.Color(45, 45, 45));

    btnHelp.setBackground(new java.awt.Color(181, 181, 181));
    btnHelp.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
    btnHelp.setForeground(new java.awt.Color(68, 68, 68));
    btnHelp.setText("Help");
    btnHelp.setBorder(null);
    btnHelp.setFocusPainted(false);
    btnHelp.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnHelpActionPerformed(evt);
        }
    });

    btnAbout.setBackground(new java.awt.Color(181, 181, 181));
    btnAbout.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
    btnAbout.setForeground(new java.awt.Color(68, 68, 68));
    btnAbout.setText("About");
    btnAbout.setBorder(null);
    btnAbout.setFocusPainted(false);
    btnAbout.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnAboutActionPerformed(evt);
        }
    });

    btnLogout.setBackground(new java.awt.Color(181, 181, 181));
    btnLogout.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
    btnLogout.setForeground(new java.awt.Color(68, 68, 68));
    btnLogout.setText("Log-out");
    btnLogout.setBorder(null);
    btnLogout.setFocusPainted(false);
    btnLogout.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnLogoutActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout pnlSettingsButtonsHolderLayout = new javax.swing.GroupLayout(pnlSettingsButtonsHolder);
    pnlSettingsButtonsHolder.setLayout(pnlSettingsButtonsHolderLayout);
    pnlSettingsButtonsHolderLayout.setHorizontalGroup(
        pnlSettingsButtonsHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlSettingsButtonsHolderLayout.createSequentialGroup()
            .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE))
        .addGroup(pnlSettingsButtonsHolderLayout.createSequentialGroup()
            .addGap(63, 63, 63)
            .addGroup(pnlSettingsButtonsHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lblSettingsHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(61, Short.MAX_VALUE))
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSettingsButtonsHolderLayout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(pnlSettingsButtonsHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(btnHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btnAbout, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    pnlSettingsButtonsHolderLayout.setVerticalGroup(
        pnlSettingsButtonsHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlSettingsButtonsHolderLayout.createSequentialGroup()
            .addGap(17, 17, 17)
            .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(lblSettingsHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(94, 94, 94)
            .addComponent(btnHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(40, 40, 40)
            .addComponent(btnAbout, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(40, 40, 40)
            .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout SettingsPanelLayout = new javax.swing.GroupLayout(SettingsPanel);
    SettingsPanel.setLayout(SettingsPanelLayout);
    SettingsPanelLayout.setHorizontalGroup(
        SettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SettingsPanelLayout.createSequentialGroup()
            .addComponent(pnlSettingsButtonsHolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(0, 0, 0)
            .addComponent(pnlSettingContent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
    SettingsPanelLayout.setVerticalGroup(
        SettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(pnlSettingContent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(pnlSettingsButtonsHolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );

    getContentPane().add(SettingsPanel, "settingscard");

    Contacts.setBackground(new java.awt.Color(255, 255, 255));

    pnlSearchHolder.setBackground(new java.awt.Color(217, 217, 217));
    pnlSearchHolder.setForeground(new java.awt.Color(148, 148, 148));

    btnBackContacts.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UI/resources/backArrow.png"))); // NOI18N
    btnBackContacts.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    btnBackContacts.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            btnBackContactsMouseClicked(evt);
        }
    });

    lblContactsHeader.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UI/resources/contactsheader.png"))); // NOI18N

    pnlContactsHolder.setBackground(new java.awt.Color(255, 255, 255));

    javax.swing.GroupLayout pnlContactsHolderLayout = new javax.swing.GroupLayout(pnlContactsHolder);
    pnlContactsHolder.setLayout(pnlContactsHolderLayout);
    pnlContactsHolderLayout.setHorizontalGroup(
        pnlContactsHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 378, Short.MAX_VALUE)
    );
    pnlContactsHolderLayout.setVerticalGroup(
        pnlContactsHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 551, Short.MAX_VALUE)
    );

    pnlContactsHolderFooter.setBackground(new java.awt.Color(51, 51, 51));

    javax.swing.GroupLayout pnlContactsHolderFooterLayout = new javax.swing.GroupLayout(pnlContactsHolderFooter);
    pnlContactsHolderFooter.setLayout(pnlContactsHolderFooterLayout);
    pnlContactsHolderFooterLayout.setHorizontalGroup(
        pnlContactsHolderFooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 0, Short.MAX_VALUE)
    );
    pnlContactsHolderFooterLayout.setVerticalGroup(
        pnlContactsHolderFooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 20, Short.MAX_VALUE)
    );

    javax.swing.GroupLayout pnlSearchHolderLayout = new javax.swing.GroupLayout(pnlSearchHolder);
    pnlSearchHolder.setLayout(pnlSearchHolderLayout);
    pnlSearchHolderLayout.setHorizontalGroup(
        pnlSearchHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlSearchHolderLayout.createSequentialGroup()
            .addComponent(btnBackContacts, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE))
        .addGroup(pnlSearchHolderLayout.createSequentialGroup()
            .addContainerGap(82, Short.MAX_VALUE)
            .addGroup(pnlSearchHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(lblContactsHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(pnlContactsHolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlContactsHolderFooter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap(83, Short.MAX_VALUE))
    );
    pnlSearchHolderLayout.setVerticalGroup(
        pnlSearchHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlSearchHolderLayout.createSequentialGroup()
            .addGap(17, 17, 17)
            .addComponent(btnBackContacts, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(lblContactsHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(pnlContactsHolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, 0)
            .addComponent(pnlContactsHolderFooter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    pnlContactsHolder.setLayout(new BorderLayout());
    pnlContactsHolder.add(new ContactSelection(pmc));

    pnlContactType.setBackground(new java.awt.Color(204, 0, 0));

    lblContactType.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
    lblContactType.setForeground(new java.awt.Color(255, 255, 255));
    lblContactType.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

    javax.swing.GroupLayout pnlContactTypeLayout = new javax.swing.GroupLayout(pnlContactType);
    pnlContactType.setLayout(pnlContactTypeLayout);
    pnlContactTypeLayout.setHorizontalGroup(
        pnlContactTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(lblContactType, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
    pnlContactTypeLayout.setVerticalGroup(
        pnlContactTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlContactTypeLayout.createSequentialGroup()
            .addContainerGap(16, Short.MAX_VALUE)
            .addComponent(lblContactType, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(17, 17, 17))
    );

    pnlContactsSwitch.setLayout(new java.awt.CardLayout());

    pnlNoContactSelected.setBackground(new java.awt.Color(255, 255, 255));

    jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UI/resources/contactslarge.png"))); // NOI18N

    jLabel5.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
    jLabel5.setForeground(new java.awt.Color(102, 102, 102));
    jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel5.setText("No Contact Selected");

    javax.swing.GroupLayout pnlNoContactSelectedLayout = new javax.swing.GroupLayout(pnlNoContactSelected);
    pnlNoContactSelected.setLayout(pnlNoContactSelectedLayout);
    pnlNoContactSelectedLayout.setHorizontalGroup(
        pnlNoContactSelectedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlNoContactSelectedLayout.createSequentialGroup()
            .addContainerGap(121, Short.MAX_VALUE)
            .addGroup(pnlNoContactSelectedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap(103, Short.MAX_VALUE))
    );
    pnlNoContactSelectedLayout.setVerticalGroup(
        pnlNoContactSelectedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlNoContactSelectedLayout.createSequentialGroup()
            .addGap(168, 168, 168)
            .addComponent(jLabel4)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel5)
            .addContainerGap(231, Short.MAX_VALUE))
    );

    pnlContactsSwitch.add(pnlNoContactSelected, "no");

    pnlContactView.setBackground(new java.awt.Color(255, 255, 255));

    lblContactUsername.setFont(new java.awt.Font("Arial", 1, 36)); // NOI18N
    lblContactUsername.setForeground(new java.awt.Color(84, 84, 84));
    lblContactUsername.setText("User Name");

    lblPublicKeyHeader.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
    lblPublicKeyHeader.setForeground(new java.awt.Color(11, 11, 11));
    lblPublicKeyHeader.setText("Public Key Hash SHA-1:");

    lblContactFriendlyName.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
    lblContactFriendlyName.setForeground(new java.awt.Color(153, 153, 153));
    lblContactFriendlyName.setText("(Friendly Name)");
    lblContactFriendlyName.setMaximumSize(new java.awt.Dimension(380, 29));

    pnlAddtionalInfoHolder.setBackground(new java.awt.Color(221, 221, 221));

    jScrollPane1.setBackground(new java.awt.Color(204, 204, 204));

    txtAdditionalInformation.setBackground(new java.awt.Color(221, 221, 221));
    txtAdditionalInformation.setColumns(10);
    txtAdditionalInformation.setFont(new java.awt.Font("Arial Unicode MS", 0, 18)); // NOI18N
    txtAdditionalInformation.setForeground(new java.awt.Color(51, 51, 51));
    txtAdditionalInformation.setLineWrap(true);
    txtAdditionalInformation.setRows(1);
    txtAdditionalInformation.setWrapStyleWord(true);
    txtAdditionalInformation.setBorder(null);
    txtAdditionalInformation.setMargin(new java.awt.Insets(5, 0, 5, 8));
    txtAdditionalInformation.addFocusListener(new java.awt.event.FocusAdapter() {
        public void focusGained(java.awt.event.FocusEvent evt) {
            txtAdditionalInformationFocusGained(evt);
        }
        public void focusLost(java.awt.event.FocusEvent evt) {
            txtAdditionalInformationFocusLost(evt);
        }
    });
    txtAdditionalInformation.addKeyListener(new java.awt.event.KeyAdapter() {
        public void keyReleased(java.awt.event.KeyEvent evt) {
            txtAdditionalInformationKeyReleased(evt);
        }
        public void keyTyped(java.awt.event.KeyEvent evt) {
            txtAdditionalInformationKeyTyped(evt);
        }
    });
    jScrollPane1.setViewportView(txtAdditionalInformation);

    javax.swing.GroupLayout pnlAddtionalInfoHolderLayout = new javax.swing.GroupLayout(pnlAddtionalInfoHolder);
    pnlAddtionalInfoHolder.setLayout(pnlAddtionalInfoHolderLayout);
    pnlAddtionalInfoHolderLayout.setHorizontalGroup(
        pnlAddtionalInfoHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
    );
    pnlAddtionalInfoHolderLayout.setVerticalGroup(
        pnlAddtionalInfoHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
    );

    lblAdditionalInformationHeader.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
    lblAdditionalInformationHeader.setForeground(new java.awt.Color(11, 11, 11));
    lblAdditionalInformationHeader.setText("Additional Information:");

    lblPublicKeyHash.setForeground(new java.awt.Color(102, 102, 102));
    lblPublicKeyHash.setText("2507B25614F2AA2FD1ED04945FEF35BB99F621F6");

    lblPublicKeyExpiryHeader.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
    lblPublicKeyExpiryHeader.setForeground(new java.awt.Color(11, 11, 11));
    lblPublicKeyExpiryHeader.setText("Public Key Expires in:");

    lblPublicKeyExpiryDate.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
    lblPublicKeyExpiryDate.setForeground(new java.awt.Color(102, 102, 102));
    lblPublicKeyExpiryDate.setText("21 days, 12 hours");

    btnCreateChatWithUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UI/resources/message button.png"))); // NOI18N
    btnCreateChatWithUser.setToolTipText("Chat With User");
    btnCreateChatWithUser.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    btnCreateChatWithUser.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            btnCreateChatWithUserMouseClicked(evt);
        }
    });

    btnEditFriendlyName.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UI/resources/edit.gif"))); // NOI18N
    btnEditFriendlyName.setToolTipText("Edit Friendly Name");
    btnEditFriendlyName.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    btnEditFriendlyName.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            btnEditFriendlyNameMouseClicked(evt);
        }
    });

    jLabel13.setText("Edit Contact Group:");

    btnEditContactGroup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UI/resources/edit.gif"))); // NOI18N
    btnEditContactGroup.setToolTipText("Edit Contact Group (Name + Colour)");
    btnEditContactGroup.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    btnEditContactGroup.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            btnEditContactGroupMouseClicked(evt);
        }
    });

    javax.swing.GroupLayout pnlContactViewLayout = new javax.swing.GroupLayout(pnlContactView);
    pnlContactView.setLayout(pnlContactViewLayout);
    pnlContactViewLayout.setHorizontalGroup(
        pnlContactViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlContactViewLayout.createSequentialGroup()
            .addGap(30, 30, 30)
            .addGroup(pnlContactViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlContactViewLayout.createSequentialGroup()
                    .addGap(6, 6, 6)
                    .addGroup(pnlContactViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblPublicKeyHash)
                        .addComponent(lblPublicKeyHeader)
                        .addGroup(pnlContactViewLayout.createSequentialGroup()
                            .addGroup(pnlContactViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblPublicKeyExpiryHeader)
                                .addComponent(lblPublicKeyExpiryDate))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnCreateChatWithUser))))
                .addGroup(pnlContactViewLayout.createSequentialGroup()
                    .addGroup(pnlContactViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblAdditionalInformationHeader)
                        .addComponent(pnlAddtionalInfoHolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblContactUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 341, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(pnlContactViewLayout.createSequentialGroup()
                            .addComponent(lblContactFriendlyName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(5, 5, 5)
                            .addComponent(btnEditFriendlyName, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGap(0, 30, Short.MAX_VALUE)))
            .addContainerGap())
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlContactViewLayout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel13)
            .addGap(0, 0, 0)
            .addComponent(btnEditContactGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
    pnlContactViewLayout.setVerticalGroup(
        pnlContactViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlContactViewLayout.createSequentialGroup()
            .addGroup(pnlContactViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlContactViewLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel13))
                .addComponent(btnEditContactGroup))
            .addGap(0, 0, 0)
            .addComponent(lblContactUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(pnlContactViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(lblContactFriendlyName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnEditFriendlyName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(37, 37, 37)
            .addComponent(lblAdditionalInformationHeader)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(pnlAddtionalInfoHolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(pnlContactViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlContactViewLayout.createSequentialGroup()
                    .addGap(49, 49, 49)
                    .addComponent(lblPublicKeyHeader)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(lblPublicKeyHash, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(lblPublicKeyExpiryHeader)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(lblPublicKeyExpiryDate)
                    .addContainerGap(107, Short.MAX_VALUE))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlContactViewLayout.createSequentialGroup()
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCreateChatWithUser)
                    .addContainerGap())))
    );

    pnlContactsSwitch.add(pnlContactView, "normal");

    pnlOnlineContactView.setBackground(new java.awt.Color(255, 255, 255));

    lblOnlineContactUsername.setFont(new java.awt.Font("Arial", 1, 36)); // NOI18N
    lblOnlineContactUsername.setForeground(new java.awt.Color(84, 84, 84));
    lblOnlineContactUsername.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblOnlineContactUsername.setText("User Name");

    btnNewChat.setBackground(new java.awt.Color(181, 181, 181));
    btnNewChat.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
    btnNewChat.setForeground(new java.awt.Color(68, 68, 68));
    btnNewChat.setText("New Chat");
    btnNewChat.setBorder(null);
    btnNewChat.setFocusPainted(false);
    btnNewChat.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnNewChatActionPerformed(evt);
        }
    });

    btnAddContact.setBackground(new java.awt.Color(181, 181, 181));
    btnAddContact.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
    btnAddContact.setForeground(new java.awt.Color(68, 68, 68));
    btnAddContact.setText("Add to Contacts");
    btnAddContact.setBorder(null);
    btnAddContact.setFocusPainted(false);
    btnAddContact.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnAddContactActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout pnlOnlineContactViewLayout = new javax.swing.GroupLayout(pnlOnlineContactView);
    pnlOnlineContactView.setLayout(pnlOnlineContactViewLayout);
    pnlOnlineContactViewLayout.setHorizontalGroup(
        pnlOnlineContactViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlOnlineContactViewLayout.createSequentialGroup()
            .addContainerGap(43, Short.MAX_VALUE)
            .addGroup(pnlOnlineContactViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(btnAddContact, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                .addComponent(lblOnlineContactUsername, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnNewChat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap(43, Short.MAX_VALUE))
    );
    pnlOnlineContactViewLayout.setVerticalGroup(
        pnlOnlineContactViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(pnlOnlineContactViewLayout.createSequentialGroup()
            .addGap(112, 112, 112)
            .addComponent(lblOnlineContactUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(37, 37, 37)
            .addComponent(btnNewChat, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(29, 29, 29)
            .addComponent(btnAddContact, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(334, Short.MAX_VALUE))
    );

    pnlContactsSwitch.add(pnlOnlineContactView, "online");

    javax.swing.GroupLayout ContactsLayout = new javax.swing.GroupLayout(Contacts);
    Contacts.setLayout(ContactsLayout);
    ContactsLayout.setHorizontalGroup(
        ContactsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ContactsLayout.createSequentialGroup()
            .addComponent(pnlSearchHolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(ContactsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pnlContactType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(ContactsLayout.createSequentialGroup()
                    .addComponent(pnlContactsSwitch, javax.swing.GroupLayout.PREFERRED_SIZE, 464, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE))))
    );
    ContactsLayout.setVerticalGroup(
        ContactsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(pnlSearchHolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(ContactsLayout.createSequentialGroup()
            .addComponent(pnlContactType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, 0)
            .addComponent(pnlContactsSwitch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    getContentPane().add(Contacts, "contactscard");

    pack();
    setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnContactsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnContactsMouseClicked
        // TODO add your handling code here:
        contactsShow();
    }//GEN-LAST:event_btnContactsMouseClicked

    private void contactsShow() {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "contactscard");

        CardLayout cl2 = (CardLayout) pnlContactsSwitch.getLayout();
        cl2.show(pnlContactsSwitch, "no");

        pnlContactType.setBackground(new Color(48, 108, 147));
        lblContactType.setText("");

        reloadContactsSelection();
    }

    private void txtAdditionalInformationKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAdditionalInformationKeyTyped
        
        

    }//GEN-LAST:event_txtAdditionalInformationKeyTyped

    private void txtAdditionalInformationFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtAdditionalInformationFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAdditionalInformationFocusLost

    private void txtAdditionalInformationFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtAdditionalInformationFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAdditionalInformationFocusGained

    private void btnBackMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBackMouseClicked
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "maincard");
    }//GEN-LAST:event_btnBackMouseClicked

    private void btnSettingsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSettingsMouseClicked
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "settingscard");

        CardLayout cl2 = (CardLayout) pnlSettingArea.getLayout();
        cl2.show(pnlSettingArea, "SelectSetting");

        lblSettingName.setText("");
        lblSettingDescription.setText("Select A Setting...");


    }//GEN-LAST:event_btnSettingsMouseClicked

    private void btnHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHelpActionPerformed
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) pnlSettingArea.getLayout();
        cl.show(pnlSettingArea, "Help");

        lblSettingName.setText("Help");
        lblSettingDescription.setText("Help Documentation & Support");
    }//GEN-LAST:event_btnHelpActionPerformed

    private void btnAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAboutActionPerformed
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) pnlSettingArea.getLayout();
        cl.show(pnlSettingArea, "About");

        lblSettingName.setText("About");
        lblSettingDescription.setText("Infomation about Pengu Messenger");
    }//GEN-LAST:event_btnAboutActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) pnlSettingArea.getLayout();
        cl.show(pnlSettingArea, "Logout");

        lblSettingName.setText("Log-out");
        lblSettingDescription.setText("");
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnBackContactsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBackContactsMouseClicked
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "maincard");
    }//GEN-LAST:event_btnBackContactsMouseClicked

    private void btnNewChatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewChatActionPerformed
        // TODO add your handling code here:
        pmc.addContact(onlineContactToAdd.toLowerCase());

        CardLayout cl = (CardLayout) getContentPane().getLayout();

        cl.show(getContentPane(), "maincard");

        pmc.getChatID(chatTarget);
        activeChatSelection.setSelected(chatTarget);

        //addMessageBox(chatTarget, selectedChatID);
        //addMessageBox(onlineContactToAdd.toLowerCase(), selectedChatID);

    }//GEN-LAST:event_btnNewChatActionPerformed

    private void btnAddContactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddContactActionPerformed
        // TODO add your handling code here:
        pmc.addContact(onlineContactToAdd.toLowerCase());
        pnlContactsHolder.removeAll();
        pnlContactsHolder.add(new ContactSelection(pmc) {
            @Override
            public void showProperties() {
                loadSelectedContact(getSelected()); //To change body of generated methods, choose Tools | Templates.
            }

        });
        pnlContactsHolder.revalidate();
        loadSelectedContact(onlineContactToAdd.toLowerCase());
    }//GEN-LAST:event_btnAddContactActionPerformed

    private void btnCreateChatWithUserMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCreateChatWithUserMouseClicked
        // TODO add your handling code here
        pmc.getChatID(chatTarget);
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "maincard");
        //int chatID = pmc.getChatID(chatTarget);
        //addMessageBox(chatTarget, chatID);
        activeChatSelection.setSelected(chatTarget);
        activeChatSelection.selectedAction();


    }//GEN-LAST:event_btnCreateChatWithUserMouseClicked

    private void btnSendMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSendMouseClicked
        validateSend();
    }//GEN-LAST:event_btnSendMouseClicked

    private void btnEditFriendlyNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEditFriendlyNameMouseClicked
        editChatTargetFriendlyName();
    }//GEN-LAST:event_btnEditFriendlyNameMouseClicked

    private void btnLogoutConfirmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutConfirmActionPerformed
        LogoutAction("Logged out successfully", new Color(29, 99, 69));
    }//GEN-LAST:event_btnLogoutConfirmActionPerformed

    private void btnEditContactGroupMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEditContactGroupMouseClicked
        editChatTargetContactGroup();
    }//GEN-LAST:event_btnEditContactGroupMouseClicked

    private void txtAdditionalInformationKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAdditionalInformationKeyReleased
        pmc.editContactAdditionalInformation(chatTarget, txtAdditionalInformation.getText());
    }//GEN-LAST:event_txtAdditionalInformationKeyReleased

    private void lblLinkHelpMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblLinkHelpMouseClicked
        try {
            // TODO add your handling code here:
            openWebpage(new URL("https://www.diorb.com/pengumessenger/help"));
        } catch (MalformedURLException ex) {
            System.out.println(ex);
        }
    }//GEN-LAST:event_lblLinkHelpMouseClicked

    private void lblAboutLinkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblAboutLinkMouseClicked
        try {
            // TODO add your handling code here:
            openWebpage(new URL("https://www.diorb.com/pengumessenger/about"));
        } catch (MalformedURLException ex) {
            System.out.println(ex);
        }
    }//GEN-LAST:event_lblAboutLinkMouseClicked
    
    private static boolean openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
                return true;
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
        return false;
    }

    private static boolean openWebpage(URL url) {
        try {
            return openWebpage(url.toURI());
        } catch (URISyntaxException ex) {
            System.out.println(ex);
        }
        return false;
    }
  

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Contacts;
    private javax.swing.JPanel MainView;
    private javax.swing.JPanel SettingsPanel;
    private javax.swing.JButton btnAbout;
    private javax.swing.JButton btnAddContact;
    private javax.swing.JLabel btnBack;
    private javax.swing.JLabel btnBackContacts;
    private javax.swing.JLabel btnContacts;
    private javax.swing.JLabel btnCreateChatWithUser;
    private javax.swing.JLabel btnEditContactGroup;
    private javax.swing.JLabel btnEditFriendlyName;
    private javax.swing.JButton btnHelp;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnLogoutConfirm;
    private javax.swing.JButton btnNewChat;
    private javax.swing.JLabel btnSend;
    private javax.swing.JLabel btnSettings;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JList<String> jList1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel lblAboutHeader;
    private javax.swing.JLabel lblAboutLink;
    private javax.swing.JLabel lblActiveChatFriendlyName;
    private javax.swing.JLabel lblActiveChatUsername;
    private javax.swing.JLabel lblAdditionalInformationHeader;
    private javax.swing.JLabel lblClientOnlineStatus;
    private javax.swing.JLabel lblContactFriendlyName;
    private javax.swing.JLabel lblContactType;
    private javax.swing.JLabel lblContactUsername;
    private javax.swing.JLabel lblContactsHeader;
    private javax.swing.JLabel lblDevelopedBy;
    private javax.swing.JLabel lblDevelopedByHeader;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblEmailAddress;
    private javax.swing.JLabel lblForHelpHeader;
    private javax.swing.JLabel lblLinkHelp;
    private javax.swing.JLabel lblOnlineContactUsername;
    private javax.swing.JLabel lblPenguMessengerHeader;
    private javax.swing.JLabel lblPublicKeyExpiryDate;
    private javax.swing.JLabel lblPublicKeyExpiryHeader;
    private javax.swing.JLabel lblPublicKeyHash;
    private javax.swing.JLabel lblPublicKeyHeader;
    private javax.swing.JLabel lblReleaseDate;
    private javax.swing.JLabel lblReleaseDateHeader;
    private javax.swing.JLabel lblSettingDescription;
    private javax.swing.JLabel lblSettingName;
    private javax.swing.JLabel lblSettingsAnimation;
    private javax.swing.JLabel lblSettingsHeader;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JLabel lblVersionHeader;
    private javax.swing.JLabel lblWebHeader;
    private javax.swing.JPanel pnlAbout;
    private javax.swing.JPanel pnlActiveChatsHolder;
    private javax.swing.JPanel pnlActivity;
    private javax.swing.JPanel pnlAddtionalInfoHolder;
    private javax.swing.JPanel pnlBar;
    private javax.swing.JPanel pnlContactType;
    private javax.swing.JPanel pnlContactView;
    private javax.swing.JPanel pnlContactsHolder;
    private javax.swing.JPanel pnlContactsHolderFooter;
    private javax.swing.JPanel pnlContactsSwitch;
    private javax.swing.JPanel pnlGreenHead;
    private javax.swing.JPanel pnlHelp;
    private javax.swing.JPanel pnlInfoHeader;
    private javax.swing.JPanel pnlLogout;
    private javax.swing.JPanel pnlMessageInput;
    private javax.swing.JPanel pnlNoContactSelected;
    private javax.swing.JPanel pnlOnlineContactView;
    private javax.swing.JPanel pnlRoundMessageHolder;
    private javax.swing.JPanel pnlSearchHolder;
    private javax.swing.JPanel pnlSelectSetting;
    private javax.swing.JPanel pnlSettingArea;
    private javax.swing.JPanel pnlSettingContent;
    private javax.swing.JPanel pnlSettingsButtonsHolder;
    private javax.swing.JTextArea txtAdditionalInformation;
    private javax.swing.JTextArea txtMessageInput;
    // End of variables declaration//GEN-END:variables
}
