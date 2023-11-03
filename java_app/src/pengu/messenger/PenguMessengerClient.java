package pengu.messenger;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.ParseException;
import pengu.database.ContactsDBCommunicator;
import pengu.database.DBSetup;
import pengu.database.MessageDBCommunicator;
import pengu.messenger.PenguMessengerRESTClient.PublicKey;
import pengu.messenger.PenguRESTClient.BadConnectionException;
import pengu.messenger.PenguRESTClient.BadRequestException;
import pengu.messenger.PenguRESTClient.ForbiddenException;
import pengu.messenger.PenguRESTClient.NotFoundException;
import pengu.messenger.PenguRESTClient.ServerErrorException;
import pengu.messenger.PenguRESTClient.ServiceDownException;
import pengu.messenger.PenguRESTClient.UnauthorisedException;
import pengusecurity.encrypt.PenguEncryptedMessage;
import pengusecurity.encrypt.PenguKeyPair;

/**
 * This class provides all the methods necessary for the Pengu Messenger
 * application. It also provides an interface between the GUI code and Working
 * code.
 *
 * @author Shivun Chinniah
 */
public class PenguMessengerClient {

    /**
     * Stores the session's account access token.
     */
    private String token;

    /**
     * Stores the session's account username.
     */
    private String username;

    /**
     * Stores the rest client required to communicate with the server.
     */
    private final PenguMessengerRESTClient restClient;

    /**
     * Stores the Database communicator required to communicate with the contact
     * related databases.
     */
    private ContactsDBCommunicator cdbc;

    /**
     * Stores the Database communicator required to communicate with the message
     * related databases.
     */
    private MessageDBCommunicator mdbc;

    /**
     * Stores an updated list of inbound message references to prevent
     * downloading the same message multiple times.
     */
    private ArrayList<String> messageReferences;

    /**
     * Default Constructor method.
     */
    public PenguMessengerClient() {
        messageReferences = new ArrayList<>();
        cdbc = new ContactsDBCommunicator(DBSetup.BASE_DIR + DBSetup.APP_DB);
        mdbc = new MessageDBCommunicator(DBSetup.BASE_DIR + DBSetup.APP_DB);
        restClient = new PenguMessengerRESTClient();

    }

    /**
     * Accessor method for the username field.
     *
     * @return Returns String value of the username field.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Helper method that creates the user's data
     *
     */
    private void makeUserDatabase() {
        File dbFile = new File(DBSetup.BASE_DIR + DBSetup.getAppDBForUser(username));

        if (dbFile.canRead()) {//File exists
            //do nothing
            // System.out.println(dbFile.getAbsolutePath());
        } else { //File does not exits
            try {
                Files.copy(new File(DBSetup.BASE_DIR + DBSetup.APP_DB_TEMPLATE).toPath(), dbFile.toPath()); // Creat copy of template database
            } catch (IOException ex) {
                System.out.println(ex + " @ makeUserDatabase");
                //JOptionPane.showMessageDialog(null, ex);
            }
        }
    }

    /**
     * Exception class for any server related problem, excluding Forbidden and
     * BadRequest
     */
    public static class ServerProblemException extends Exception {

        /**
         * Stores the description of the server problem.
         */
        String description;

        /**
         * Parameterized constructor method.
         *
         * @param description The description of the server problem.
         */
        public ServerProblemException(String description) {
            this.description = description;
        }

        /**
         * Accessor method for the description field.
         *
         * @return Returns String value of description field.
         */
        public String getDescription() {
            return description;
        }

    }

    /**
     * Makes a login attempt with the specified credentials.
     *
     * @param username The account username.
     * @param password The account password
     * @throws pengu.messenger.PenguRESTClient.ForbiddenException
     * @throws pengu.messenger.PenguMessengerClient.ServerProblemException
     * @throws pengu.messenger.PenguRESTClient.UnauthorisedException
     */
    public void Login(String username, String password) throws ForbiddenException, ServerProblemException, UnauthorisedException {

        try {
            this.token = restClient.getToken(username, password);

            //if successful
            this.username = username;
            makeUserDatabase();

            cdbc = new ContactsDBCommunicator(DBSetup.BASE_DIR + DBSetup.getAppDBForUser(username));
            mdbc = new MessageDBCommunicator(DBSetup.BASE_DIR + DBSetup.getAppDBForUser(username));

            doAtStart();
            loadDownloadedMessageReferencesToArray();

            //else catch (not successful)
        } catch (ServerErrorException ex) {
            throw new ServerProblemException("Server Error.");
        } catch (BadRequestException ex) {
            throw new ServerProblemException("Bad Request.");
        } catch (NotFoundException ex) {
            throw new ServerProblemException("Not Found.");
        } catch (ServiceDownException ex) {
            throw new ServerProblemException("Service Is Down.");
        } catch (BadConnectionException ex) {
            throw new ServerProblemException("Could Not Connect to Server, Check Network Connection");
        }

    }

    /**
     * Makes a search for users and their public keys.
     *
     * @param username The username (Search Term).
     * @return Returns a PublicKey array of search results. If no search is
     * found, the array length is 0.
     * @throws pengu.messenger.PenguMessengerClient.ServerProblemException
     */
    public PublicKey[] searchForOtherUsers(String username) throws ServerProblemException {
        try {
            return restClient.searchUsers(username, 1);
        } catch (ServerErrorException ex) {
            throw new ServerProblemException("Server Error.");
        } catch (BadRequestException ex) {
            throw new ServerProblemException("Bad Request.");
        } catch (NotFoundException ex) {
            throw new ServerProblemException("Not Found.");
        } catch (ServiceDownException ex) {
            throw new ServerProblemException("Service Is Down.");
        } catch (BadConnectionException ex) {
            throw new ServerProblemException("Could Not Connect to Server, Check Network Connection");
        } catch (UnauthorisedException | ParseException | java.text.ParseException ex) {
            // Do nothing should not happen
        }

        return new PublicKey[0];
    }

    /**
     * Makes a registration attempt with the specified credentials.S
     *
     * @param username The new account username.
     * @param email The new account email.
     * @param password The new account password.
     * @throws pengu.messenger.PenguRESTClient.ForbiddenException
     * @throws pengu.messenger.PenguMessengerClient.ServerProblemException
     */
    public void Register(String username, String email, String password) throws ForbiddenException, ServerProblemException {

        try {
            restClient.registerUser(username, password, email);
        } catch (ServerErrorException ex) {
            throw new ServerProblemException("Server Error.");
        } catch (BadRequestException ex) {
            throw new ServerProblemException("Bad Request.");
        } catch (NotFoundException ex) {
            throw new ServerProblemException("Not Found.");
        } catch (ServiceDownException ex) {
            throw new ServerProblemException("Service Is Down.");
        } catch (BadConnectionException ex) {
            throw new ServerProblemException("Could Not Connect to Server, Check Network Connection");
        }

    }

    //Database -- Contacts
    /**
     * This class allows for contact objects to be created so that contacts
     * information can be stored in an array. It stores details about a contact:
     * the user ID, the username, the display/friendly name, the contact group,
     * the contact group color, additional information, the public key and the
     * expiry date of the public key.
     */
    public static class ContactItem {

        /**
         * Stores the user's database ID.
         */
        private int userID;

        /**
         * Stores the user's username.
         */
        private final String username;

        /**
         * Stores the user's display/friendly name.
         */
        private final String displayName;

        /**
         * Stores the user's contact group.
         */
        private final String contactGroup;

        /**
         * Stores the user's contact group Color.
         */
        private final Color contactGroupColor;

        /**
         * Stores the user's additional information.
         */
        private String additionalInformation;

        /**
         * Stores the user's newest public key.
         */
        private String publicKey;

        /**
         * Stores the user's newest public key expiry Date.
         */
        private Date publicKeyExpiryDate;

        /**
         * Parameterized constructor method. It receives: the user ID, the
         * username, the display/friendly name, the contact group, and the
         * contact group color.
         *
         * @param username The user's username.
         * @param displayName The user's display name.
         * @param contactGroup The user's contact group.
         * @param contactGroupColor The user's contact group Color.
         */
        public ContactItem(String username, String displayName, String contactGroup, Color contactGroupColor) {
            this.username = username;

            this.displayName = displayName;
            this.contactGroup = contactGroup;
            this.contactGroupColor = contactGroupColor;
        }

        /**
         * Parameterized constructor method. It receives: the user ID, the
         * username, the display/friendly name, the contact group, the contact
         * group color, additional information, the user's public key and public
         * key expiry date.
         *
         * @param username The user's username.
         * @param displayName The user's display name.
         * @param contactGroup The user's contact group.
         * @param contactGroupColor The user's contact group Color.
         * @param userID The user's ID.
         * @param additionalInformation The user's additional information.
         * @param publicKey The user's public key.
         * @param publicKeyExpiryDate The user's public key expiry Date.
         */
        public ContactItem(String username, String displayName, String contactGroup, Color contactGroupColor, int userID, String additionalInformation, String publicKey, Date publicKeyExpiryDate) {
            this.username = username;

            this.displayName = displayName;
            this.contactGroup = contactGroup;
            this.contactGroupColor = contactGroupColor;
            this.userID = userID;
            this.additionalInformation = additionalInformation;
            this.publicKey = publicKey;
            this.publicKeyExpiryDate = publicKeyExpiryDate;
        }

        /**
         * Accessor method for the publicKey field.
         *
         * @return Returns value of the publicKey field.
         */
        public String getPublicKey() {
            return publicKey;
        }

        /**
         * Accessor method for the publicKeyExpiryDate field.
         *
         * @return Returns value of the publicKeyExpiryDate field.
         */
        public Date getPublicKeyExpiryDate() {
            return publicKeyExpiryDate;
        }

        /**
         * Accessor method for the userID field.
         *
         * @return Returns value of the userID field.
         */
        public int getUserID() {
            return userID;
        }

        /**
         * Accessor method for the additionalInformation field.
         *
         * @return Returns value of the additionalInformation field.
         */
        public String getAdditionalInformation() {
            return additionalInformation;
        }

        /**
         * Accessor method for the contactGroup field.
         *
         * @return Returns value of the contactGroup field.
         */
        public String getContactGroup() {
            return contactGroup;
        }

        /**
         * Accessor method for the contactGroupColor field.
         *
         * @return Returns value of the contactGroupColor field.
         */
        public Color getContactGroupColor() {
            return contactGroupColor;
        }

        /**
         * Accessor method for the displayName field.
         *
         * @return Returns value of the displayName field.
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Accessor method for the username field.
         *
         * @return Returns value of the username field.
         */
        public String getUsername() {
            return username;
        }

    }

    /**
     * Fetches an array of contacts from the database.
     *
     * @return Returns a ContactItem array containing the contacts.
     */
    public ContactItem[] getListOfContacts() {
        try {

            ResultSet r = cdbc.fetchUsers();
            int len = 0;

            while (r.next()) {
                len++;
            }

            r.close();

            r = cdbc.fetchUsers();

            ContactItem[] out = new ContactItem[len];

            int i = 0;
            while (r.next()) {
                out[i] = new ContactItem(r.getString("Username"), r.getString("DisplayName"), r.getString("NamingConvention"), hex2Rgb(r.getString("HEXColourCode")));
                i++;
            }

            r.close();

            return out;

        } catch (SQLException ex) {
            System.out.println(ex);
            System.out.println("here");
            return new ContactItem[0];
        }

    }

    /**
     * Helper method converts a hexadecimal string into a Color.
     *
     * @param colorStr The hexadecimal color string e.g. "#FFFFFF".
     * @return Returns a Color object.
     */
    private static Color hex2Rgb(String colorStr) {
        return new Color(
                Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16));
    }

    /**
     * This class allows for active chat objects to be created so that active
     * chat information can be stored in an array. It stores details about an
     * active chat: the username, the display/friendly name, the contact group,
     * the contact group color, the last message preview, the last message
     * status, the chat ID, and the if the last message was read.
     */
    public static class ActiveChatItem {

        /**
         * Stores the chat's username.
         */
        private final String username;

        /**
         * Stores the chat's display/friendly name.
         */
        private final String displayName;

        /**
         * Stores the chat's last message preview.
         */
        private final String messagePreview;

        /**
         * Stores the chat's last message status.
         */
        private final String messageStatus;

        /**
         * Stores the chat's user contact group.
         */
        private final String contactGroup;

        /**
         * Stores the chat's user contact group Color.
         */
        private final Color contactColor;

        /**
         * Stores the chat's ID.
         */
        private final int chatID;

        /**
         * Stores a boolean representing whether the last message was seen/read.
         */
        private final boolean lastMessageSeen;

        /**
         * Parameterized constructor method, that receives the username, display
         * name, message preview, message status, contact group, contact group
         * color, chatID, and last message seen of an active chat.
         *
         * @param username The chat's username.
         * @param displayName The chat's display name.
         * @param messagePreview The chat's last message preview.
         * @param messageStatus The chat's last message status.
         * @param contactGroup The chat's user contact group.
         * @param contactColor The chat's user contact group Color.
         * @param chatID The chat's ID.
         * @param lastMessageSeen The boolean representing whether the last
         * message was seen/read.
         */
        public ActiveChatItem(String username, String displayName, String messagePreview, String messageStatus, String contactGroup, Color contactColor, int chatID, boolean lastMessageSeen) {
            this.username = username;
            this.displayName = displayName;
            this.messagePreview = messagePreview;
            this.messageStatus = messageStatus;
            this.chatID = chatID;
            this.contactGroup = contactGroup;
            this.contactColor = contactColor;
            this.lastMessageSeen = lastMessageSeen;

        }

        /**
         * Accessor method for the lastMessageSeen field.
         *
         * @return Returns value of the lastMessageSeen field.
         */
        public boolean getLastMessageSeen() {
            return lastMessageSeen;
        }

        /**
         * Accessor method for the displayName field.
         *
         * @return Returns value of the displayName field.
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Accessor method for the messagePreview field.
         *
         * @return Returns value of the messagePreview field.
         */
        public String getMessagePreview() {
            return messagePreview;
        }

        /**
         * Accessor method for the messageStatus field.
         *
         * @return Returns value of the messageStatus field.
         */
        public String getMessageStatus() {
            return messageStatus;
        }

        /**
         * Accessor method for the username field.
         *
         * @return Returns value of the username field.
         */
        public String getUsername() {
            return username;
        }

        /**
         * Accessor method for the contactColor field.
         *
         * @return Returns value of the contactColor field.
         */
        public Color getContactColor() {
            return contactColor;
        }

        /**
         * Accessor method for the contactGroup field.
         *
         * @return Returns value of the contactGroup field.
         */
        public String getContactGroup() {
            return contactGroup;
        }

        /**
         * Accessor method for the chatID field.
         *
         * @return Returns value of the chatID field.
         */
        public int getChatID() {
            return chatID;
        }

    }

    /**
     * Fetches an array of Active Chats from the database.
     *
     * @return Returns an ActiveChatItem array containing active chats.
     */
    public ActiveChatItem[] getListOfActiveChats() {
        try {

            ResultSet r = mdbc.getListOfChats();
            int len = 0;

            while (r.next()) {
                len++;
            }

            r.close();

            r = mdbc.getListOfChats();

            ActiveChatItem[] out = new ActiveChatItem[len];

            int i = 0;
            while (r.next()) {
                int chatID = Integer.parseInt(r.getString("ChatID"));

                String rPreview = "";
                String sPreview = "";

                long rDate = 0;
                long sDate = 0;

                ResultSet r2 = mdbc.getLastSentMessageFromChat(chatID);
                if (r2 != null && r2.next()) {
                    sPreview = r2.getString("MessageContent");
                    sDate = r2.getLong("DateSent");
                }

                if (r2 != null) {
                    r2.close();
                }

                r2 = mdbc.getLastReceivedMessageFromChat(chatID);
                if (r2 != null && r2.next()) {
                    rPreview = r2.getString("MessageContent");
                    rDate = r2.getLong("DateReceived");
                }

                if (r2 != null) {
                    r2.close();
                }

                String preview;
                String date;

                if (rDate > sDate) {
                    preview = rPreview;
                    date = rDate + "";
                } else {
                    preview = "> " + sPreview;
                    date = sDate + "";
                }

                if (date.equals("0")) {
                    date = "";
                    preview = "";
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(Long.parseLong(date));
                    DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm");

                    date = df.format(calendar.getTime());

                }

                out[i] = new ActiveChatItem(r.getString("Username"), r.getString("DisplayName"), preview, date, r.getString("NamingConvention"), hex2Rgb(r.getString("HEXColourCode")), chatID, r.getBoolean("LastMessageSeen"));

                i++;
            }

            r.close();

            return out;

        } catch (SQLException ex) {
            System.out.println(ex + "here Y");

            return new ActiveChatItem[0];
        }

    }

    /**
     * Deletes the specified active chat from the database, and all messages in
     * the chat.
     *
     * @param chatID The chat ID of the chat to delete.
     */
    public void deleteActiveChat(int chatID) {
        mdbc.deleteActiveChat(chatID);
    }

    /**
     * This class allows for Message objects to be created so that messages can
     * be stored in an array.
     *
     * It stores details about a message:<br>
     * the message ID, the message content, the message transaction date, and
     * the message chat ID.
     */
    public static class Message {

        /**
         * Stores the message content.
         */
        private final String messageContent;

        /**
         * Stores the message's chat ID.
         */
        private final int chatID;

        /**
         * Stores the message's transaction date.
         */
        private final Date messageTransactionDate;

        /**
         * Stores the message ID.
         */
        private final int messageID;

        /**
         * Parameterized constructor method, receives: the message content, the
         * message's chat ID, the message's ID,and the message's transaction
         * date.
         *
         * @param messageContent The content of the message.
         * @param chatID The ID of the message's chat.
         * @param messageTransactionDate The transaction date of the message.
         * @param messageID The ID of the message.
         */
        public Message(String messageContent, int chatID, Date messageTransactionDate, int messageID) {
            this.messageContent = messageContent;
            this.chatID = chatID;
            this.messageTransactionDate = messageTransactionDate;
            this.messageID = messageID;

        }

        /**
         * Accessor method for the messageID field.
         *
         * @return Returns value of the messageID field.
         */
        public int getMessageID() {
            return messageID;
        }

        /**
         * Accessor method for the chatID field.
         *
         * @return Returns value of the chatID field.
         */
        public int getChatID() {
            return chatID;
        }

        /**
         * Accessor method for the messageContent field.
         *
         * @return Returns value of the messageContent field.
         */
        public String getMessageContent() {
            return messageContent;
        }

        /**
         * Accessor method for the messageTransactionDate field.
         *
         * @return Returns value of the messageTransactionDate field.
         */
        public Date getMessageTransactionDate() {
            return messageTransactionDate;
        }

    }

    /**
     * This class allows for Message objects to be created so that messages can
     * be stored in an array. It extents the Message class, to add the read, and
     * delivered functionality.
     *
     * It stores additional details about a sent message:<br>
     * the read and delivery status of the sent message, and the read and
     * delivery dates of the sent message.
     */
    public static class SentMessage extends Message {

        /**
         * Stores the read status of the sent message.
         */
        private final boolean readStatus;

        /**
         * Stores the delivery status of the sent message.
         */
        private final boolean deliveredStatus;

        /**
         * Stores the read date of the sent message.
         */
        private final Date readDate;

        /**
         * Stores the delivery date of the sent message.
         */
        private final Date deliveredDate;

        /**
         * Parameterized constructor method that receives the field required for
         * a message object, with the addition of the delivery and read:
         * statuses and dates.
         *
         * @param messageContent The content of the message.
         * @param chatID The ID of the message's chat.
         * @param messageTransactionDate The transaction date of the message.
         * @param messageID The ID of the message.
         * @param readStatus The read status of the message.
         * @param deliveredStatus The delivery status of the message.
         * @param readDate The read date of the message.
         * @param deliveredDate The delivery date of the message.
         */
        public SentMessage(String messageContent, int chatID, Date messageTransactionDate, int messageID, boolean readStatus, boolean deliveredStatus, Date readDate, Date deliveredDate) {
            super(messageContent, chatID, messageTransactionDate, messageID);
            this.deliveredStatus = deliveredStatus;
            this.readStatus = readStatus;
            this.deliveredDate = deliveredDate;
            this.readDate = readDate;
        }

        /**
         * Accessor method for the readStatus field.
         *
         * @return Returns value of the readStatus field.
         */
        public boolean getReadStatus() {
            return readStatus;
        }

        /**
         * Accessor method for the deliveredStatus field.
         *
         * @return Returns value of the deliveredStatus field.
         */
        public boolean getDeliveredStatus() {
            return deliveredStatus;
        }

        /**
         * Accessor method for the deliveredDate field.
         *
         * @return Returns value of the deliveredDate field.
         */
        public Date getDeliveredDate() {
            return deliveredDate;
        }

        /**
         * Accessor method for the readDate field.
         *
         * @return Returns value of the readDate field.
         */
        public Date getReadDate() {
            return readDate;
        }

    }

    /**
     * Fetches both received and sent messages for a specified chat.
     *
     * @param chatID The chat ID to specify the chat.
     * @return Returns an array of Message (also child SentMessage) objects
     * containing the messages sorted by transaction date (oldest - newest).
     */
    public Message[] getMessagesForChat(int chatID) {
        try {

            ResultSet r = mdbc.getSentMessagesFromChat(chatID);
            int len = 0;

            while (r.next()) {
                len++;
            }

            r.close();

            int len2 = 0;

            r = mdbc.getReceivedMessagesFromChat(chatID);
            while (r.next()) {
                len2++;
            }

            r.close();

            r = mdbc.getSentMessagesFromChat(chatID);

            Message[] out = new Message[len + len2];

            int i = 0;
            while (r.next()) {

                out[i] = new SentMessage(r.getString("MessageContent"), chatID, getDateFromMilliseconds(Long.parseLong(r.getString("DateSent"))), r.getInt("SMessageID"), r.getBoolean("ReadStatus"), r.getBoolean("DeliveryStatus"), getDateFromMilliseconds(Long.parseLong(r.getString("DateRead"))), getDateFromMilliseconds(Long.parseLong(r.getString("DateDelivered"))));

                i++;
            }

            r.close();

            r = mdbc.getReceivedMessagesFromChat(chatID);

            //no need to make i = 0, will continue
            while (r.next()) {

                out[i] = new Message(r.getString("MessageContent"), chatID, getDateFromMilliseconds(Long.parseLong(r.getString("DateReceived"))), r.getInt("RMessageID"));

                i++;
            }

            r.close();

            Arrays.sort(out, new SortByTransactionDate()); // Sorts the messages by transaction date.

            return out;

        } catch (SQLException ex) {
            System.out.println(ex + " Y");

            return new Message[0];
        }
    }

    /**
     * Helper method to create a Date object from inputted milliseconds since
     * 01/01/1970
     *
     * @param millis milliseconds.
     * @return Returns the Date equivalent to the time in milliseconds since
     * 01/01/1970
     */
    private static Date getDateFromMilliseconds(long millis) {
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(millis);
        return temp.getTime();
    }

    /**
     * Helper method used for sorting an array of Message Objects by their
     * transaction dates.
     */
    private static class SortByTransactionDate implements Comparator<Message> {

        @Override
        public int compare(Message a, Message b) {
            if (a.getMessageTransactionDate().compareTo(b.getMessageTransactionDate()) > 0) {
                return 1; // if a is older than b
            } else if (a.getMessageTransactionDate().compareTo(b.getMessageTransactionDate()) < 0) {
                return -1; // if a is newer than b
            } else {
                return 0; // if the dates are equal
            }
        }
    }

    /**
     * Fetches the details of a specified contact.
     *
     * @param username The username of the contact who's details are requested.
     * @return Returns a ContactItem Object containing the user's details, If
     * the contact does not exist null is returned.
     */
    public ContactItem getDetailsForContact(String username) {
        try {

            ResultSet r = cdbc.getContactDetails(username);

            ContactItem out;

            if (r.next()) {
                ResultSet pk = cdbc.getPublicKeys(r.getInt("UserID"));
                if (pk.next()) {

                    out = new ContactItem(username, r.getString("DisplayName"), r.getString("NamingConvention"), hex2Rgb(r.getString("HEXColourCode")), r.getInt("UserID"), r.getString("AdditionalInformation"), pk.getString("PublicKeyString"), getDateFromMilliseconds(Long.parseLong(pk.getString("PublicKeyExpiryDate"))));
                    pk.close();
                } else {
                    out = new ContactItem(username, r.getString("DisplayName"), r.getString("NamingConvention"), hex2Rgb(r.getString("HEXColourCode")), r.getInt("UserID"), r.getString("AdditionalInformation"), null, null);
                }
                r.close();

                return out;

            } else {
                return null;
            }

        } catch (SQLException ex) {
            System.out.println(ex + " @ getDetailsForContact");

            return null;
        }
    }

    /**
     * Helper method that should be called after the user has successfully
     * logged-in. It keeps local private key and public key records up to date.
     *
     * @throws pengu.messenger.PenguMessengerClient.ServerProblemException
     * @throws pengu.messenger.PenguRESTClient.UnauthorisedException
     */
    private void doAtStart() throws ServerProblemException, UnauthorisedException {
        maintainPrivateKeys();
        updateContactPublicKeys();
    }

    /**
     * Helper method that maintains private key records. It creates a new
     * private key and uploads the publick key if the the private key does not
     * exist or expires in 5 days.
     *
     * @throws pengu.messenger.PenguMessengerClient.ServerProblemException
     * @throws pengu.messenger.PenguRESTClient.UnauthorisedException
     */
    private void maintainPrivateKeys() throws ServerProblemException, UnauthorisedException {
        try {
            boolean needsRenewal = true;

            try (ResultSet r = cdbc.getPrivateKeys()) {

                Calendar time = Calendar.getInstance();
                time.setTime(new Date());
                long now = time.getTimeInMillis();
                long sevenDays = 604800000;//Seven days in milliseconds
                while (r.next()) {
                    long expiryDate = Long.parseLong(r.getString("PrivateKeyExpiryDate"));
                    //System.out.println(now +" "+ (expiryDate + sevenDays));
                    if (now > (expiryDate - sevenDays)) { //should renew private key seven days earlier
                        // initally assumed that renewal is needed
                    } else {
                        needsRenewal = false;
                    }

                }
            }

            if (needsRenewal) {
                renewPrivateKey();
            }

        } catch (SQLException ex) {
            System.out.println(ex + " @ maintainPrivateKeys");
        }
    }

    /**
     * Helper method that generates a new private key and uploads the public key
     * to the server.
     *
     * @throws pengu.messenger.PenguMessengerClient.ServerProblemException
     * @throws pengu.messenger.PenguRESTClient.UnauthorisedException
     */
    private void renewPrivateKey() throws ServerProblemException, UnauthorisedException {
        try {
            PenguKeyPair kp = new PenguKeyPair();
            //System.out.println(token);
            restClient.addPublicKey(username, token, kp.publicKeyToString());

            PublicKey temp = getContactPublicKey(username);

            if (temp != null) {
                //System.out.println("running");
                cdbc.addPrivateKey(kp.privateKeyToString(), temp.getRegistrationDate(), temp.getExpiryDate(), PenguEncryptedMessage.getSHA1Hash(kp.publicKeyToString()));
            }

        } catch (NoSuchAlgorithmException ex) {
            System.out.println(ex + " @ renewPrivateKey");
        } catch (ServerErrorException ex) {
            throw new ServerProblemException("Server Error.");
        } catch (BadRequestException ex) {
            throw new ServerProblemException("Bad Request.");
        } catch (NotFoundException ex) {
            throw new ServerProblemException("Not Found.");
        } catch (ServiceDownException ex) {
            throw new ServerProblemException("Service Is Down.");
        } catch (BadConnectionException ex) {
            throw new ServerProblemException("Could Not Connect to Server, Check Network Connection");
        }
    }

    /**
     * This class allows for Contact Type(Groups) Objects to be created so that
     * they and their properties can be stored in an array with other Contact
     * Types.
     */
    public static class ContactType {

        /**
         * Stores the contact type ID.
         */
        private final int contactTypeID;

        /**
         * Stores the contact type group name.
         */
        private final String groupName;

        /**
         * Stores the contact type group Color.
         */
        private final Color contactGroupColour;

        /**
         * Parameterized constructor method that receives the contact type: ID,
         * group name, and group Color.
         *
         * @param contactTypeID The contact type ID.
         * @param groupName The contact type group name.
         * @param contactGroupColour The contact type group Color.
         */
        public ContactType(int contactTypeID, String groupName, Color contactGroupColour) {
            this.contactTypeID = contactTypeID;
            this.groupName = groupName;
            this.contactGroupColour = contactGroupColour;
        }

        /**
         * Accessor method for the contactGroupColour; field.
         *
         * @return Returns value of the contactGroupColour; field.
         */
        public Color getContactGroupColour() {
            return contactGroupColour;
        }

        /**
         * Accessor method for the contactTypeID field.
         *
         * @return Returns value of the contactTypeID field.
         */
        public int getContactTypeID() {
            return contactTypeID;
        }

        /**
         * Accessor method for the groupName field.
         *
         * @return Returns value of the groupName field.
         */
        public String getGroupName() {
            return groupName;
        }

        /**
         * The toString method for a contact type is the contact type group
         * name. This method is used when displaying/printing-out a contact type
         * Object.
         *
         * @return
         */
        @Override
        public String toString() {
            return groupName;
        }

    }

    /**
     * Fetches an array of the available contact types and contact type data.
     *
     * @return Returns a ContactType array of available contact types.
     */
    public ContactType[] getContactTypes() {
        try {
            ResultSet r = cdbc.getContactTypes();

            int len = 0;

            while (r.next()) {
                len++;
            }

            r.close();

            r = cdbc.getContactTypes();
            ContactType[] out = new ContactType[len];

            int i = 0;
            while (r.next()) {
                out[i] = new ContactType(r.getInt("ContactTypeID"), r.getString("NamingConvention"), hex2Rgb(r.getString("HexColourCode")));
                i++;
            }
            return out;

        } catch (SQLException ex) {
            System.out.println(ex + " @ getContactType");
            return new ContactType[0];
        }

    }

    /**
     * Sets the contact group of a specified user.
     *
     * @param username The contact's username.
     * @param contactTypeID The ID of the contact type.
     */
    public void setContactsGroup(String username, int contactTypeID) {
        cdbc.setContactContactType(username, contactTypeID);
    }

    /**
     * Helper method that updates all saves contacts public keys to the latest
     * version.
     */
    private void updateContactPublicKeys() {
        try {
            try (ResultSet r = cdbc.fetchUsers()) {
                while (r.next()) {

                    updateContactPublicKey(r.getString("Username"));

                }
            }

        } catch (SQLException ex) {
            System.out.println(ex + " @ updateContactPublicKeys");
        }

    }

    /**
     * Helper method that updates a specified user's public key to the latest
     * version on the server.
     *
     * @param username
     */
    private void updateContactPublicKey(String username) {

        PublicKey pk = getContactPublicKey(username);
        ContactItem temp = getDetailsForContact(username);

        if (temp != null) {
            int userID = temp.getUserID();

            if (pk != null) {
                try {

                    boolean needsUpdate;
                    try (ResultSet r = cdbc.getPublicKeys(userID)) {
                        needsUpdate = true;
                        while (r.next()) {
                            if (r.getString("PublicKeyString").equalsIgnoreCase(pk.getPublicKey())) {
                                needsUpdate = false;

                            }
                        }
                    }

                    if (needsUpdate) {
                        cdbc.addPublicKey(userID, pk.getPublicKey(), pk.getRegistrationDate(), pk.getExpiryDate());
                    }

                } catch (SQLException ex) {
                    System.out.println(ex + " @ updateContactPublicKey");
                }
            }
        }

    }

    /**
     * Helper method fetches a specified user's latest public key from the
     * server.
     *
     * @param username
     * @return
     */
    private PublicKey getContactPublicKey(String username) {
        try {
            PublicKey[] possibleUsers = searchForOtherUsers(username);
            for (PublicKey possibleUser : possibleUsers) {
                if (possibleUser.getUsername().equalsIgnoreCase(username)) {
                    return possibleUser;
                }
            }

            return null;

        } catch (ServerProblemException ex) {
            return null;
        }
    }

    /**
     * Adds a contact to the contacts database (Adds a saved contacts).
     *
     * @param username The username of the contact to add.
     */
    public void addContact(String username) {
        cdbc.addUser(username);
        updateContactPublicKey(username);
    }

    /**
     * Deletes a specified contact from the database.
     *
     * @param userID The ID of the contact/user to delete.
     */
    public void deleteContact(int userID) {
        cdbc.deleteContact(userID);
    }

    /**
     * Deletes a specified message from the sent messages database.
     *
     * @param messageID The ID of the message.
     */
    public void deleteSentMessage(int messageID) {
        mdbc.deleteSentMessage(messageID);
    }

    /**
     * Deletes a specified message from the received messages database.
     *
     * @param messageID The ID of the message.
     */
    public void deleteReceivedMessage(int messageID) {
        mdbc.deleteReceivedMessage(messageID);
    }

    /**
     * Adds a message to the out-box database.
     *
     * @param ChatID The ID of the chat to which the message belongs.
     * @param messageContent The content of the message.
     * @param username The username of the chat.
     */
    public void outboxMessage(int ChatID, String messageContent, String username) {
        try {

            int userID = cdbc.getUserIDByUsername(username);

            int publicKeyID;
            try (ResultSet r = cdbc.getPublicKeys(userID)) {
                r.next(); // get first public key - the newest public key
                publicKeyID = r.getInt("PublicKeyID");
            }

            mdbc.addSentMessage(ChatID, messageContent, publicKeyID);
        } catch (SQLException ex) {
            System.out.println(ex + " @ outboxMessage");
        }
    }

    /**
     * Fetches the chat ID for a specified user, creates a new chat if one does
     * not exist.
     *
     * @param username The username of the chat.
     * @return Returns the chat ID of the chat.
     */
    public int getChatID(String username) {
        return mdbc.getForcedChatID(username);
    }

    /**
     * Method to create a Hexadecimal encoded SHA1 hash of an input String.
     *
     * @param input The input String.
     * @return Returns a hexadecimal encoded SHA1 hash of the input String.
     */
    public static String getSHA1Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02X ", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            System.out.println(ex);
            return "";
        }
    }

    /**
     * This method is to be repeated after the user has logged-in. It downloads,
     * sends, and checks for messages.
     *
     * @return Returns an ArrayList of integers: specifying the chat IDs of the
     * chats that received changes.
     * @throws pengu.messenger.PenguRESTClient.UnauthorisedException
     * @throws pengu.messenger.PenguMessengerClient.ServerProblemException
     * @throws pengu.messenger.PenguRESTClient.ForbiddenException
     */
    public ArrayList<Integer> updateCycle() throws UnauthorisedException, ServerProblemException, ForbiddenException {
        loadInbox();

        ArrayList<Integer> out = new ArrayList<>();

        out.addAll(downloadInboxedMessages());

        out.addAll(sendOutboxedMessages());

        return out;
    }

    /**
     * Helper method that stores all inbound and downloaded message references
     * in to an ArrayList.
     */
    private void loadDownloadedMessageReferencesToArray() {
        try {
            try (ResultSet r = mdbc.getDownloadedMessageReferences()) {
                while (r.next()) {
                    String tempReference = r.getString("MessageReference");
                    messageReferences.add(tempReference);

                }
            }

        } catch (SQLException ex) {
            System.out.println(ex + " @ loadDownloadedMessageReferencesToArray");
        }
    }

    /**
     * Helper method that makes a request to the server for inbound messages.
     *
     * @throws pengu.messenger.PenguRESTClient.UnauthorisedException
     * @throws pengu.messenger.PenguMessengerClient.ServerProblemException
     */
    
    private void loadInbox() throws UnauthorisedException, ServerProblemException {

        try {
            PenguMessengerRESTClient.MessageReference[] mr = restClient.getListOfMessageReferences(username, token);

            if (mr.length > 0) {
                for (PenguMessengerRESTClient.MessageReference mr1 : mr) {
                    if (messageReferences.indexOf(mr1.getMessageReference() + "") > -1) {
                        //exists already
                        //do nothing

                    } else {
                        mdbc.addMessageInboxReference(mr1.getMessageReference(), mr1.getSender());
                        messageReferences.add(mr1.getMessageReference() + "");
                    }
                }

            }

        } catch (ServerErrorException ex) {
            throw new ServerProblemException("Server Error.");
        } catch (BadRequestException ex) {
            throw new ServerProblemException("Bad Request.");
        } catch (NotFoundException ex) {
            throw new ServerProblemException("Not Found.");
        } catch (ServiceDownException ex) {
            throw new ServerProblemException("Service Is Down.");
        } catch (BadConnectionException ex) {
            throw new ServerProblemException("Could Not Connect to Server, Check Network Connection");
        } catch (ParseException ex) {
            //do nothing
        }
    }

    /**
     * This class is used to link a private key to a public key (or public key
     * hash).
     */
    public static class PrivateKeyLink {

        /**
         * Stores the public key Hash String.
         */
        private final String publicKeyHash;

        /**
         * Stores the private key String.
         */
        private final String privateKeyString;

        /**
         * Parameterized constructor method that receives private key String and
         * public key hash.
         *
         * @param publicKeyHash The public key hash String.
         * @param privateKeyString The private key String.
         */
        public PrivateKeyLink(String publicKeyHash, String privateKeyString) {
            this.publicKeyHash = publicKeyHash;
            this.privateKeyString = privateKeyString;
        }

        /**
         * Accessor method for the privateKeyString field.
         *
         * @return Returns the value of the privateKeyString field.
         */
        public String getPrivateKeyString() {
            return privateKeyString;
        }

        /**
         * Accessor method for the publicKeyHash field.
         *
         * @return Returns the value of the publicKeyHash field.
         */
        public String getPublicKeyHash() {
            return publicKeyHash;
        }

    }

    /**
     * Fetches an array of Private keys with their Hashed public keys.
     *
     * @return Returns an array of PrivateKeyLink Objects.
     */
    private PrivateKeyLink[] getPrivateKeyWithLink() {
        try {
            ResultSet r = cdbc.getPrivateKeys();

            int len = 0;
            while (r.next()) {
                len++;
            }
            r.close();

            r = cdbc.getPrivateKeys();
            PrivateKeyLink[] out = new PrivateKeyLink[len];

            int i = 0;
            while (r.next()) {
                String publicKeyString = r.getString("PublicKeyString");
                String privateKeyString = r.getString("PrivateKeyString");
                out[i] = new PrivateKeyLink(publicKeyString, privateKeyString);
                i++;
            }
            r.close();
            return out;

        } catch (SQLException ex) {
            System.out.println(ex + "@ getPrivateKeyWithLink");
        }
        return new PrivateKeyLink[0];
    }

    /**
     * Helper method that downloads all un-downloaded messages from the server.
     *
     * @return Returns an ArrayList of integers of the affected chats, chat IDs.
     * @throws pengu.messenger.PenguRESTClient.UnauthorisedException
     * @throws pengu.messenger.PenguMessengerClient.ServerProblemException
     */
    private ArrayList<Integer> downloadInboxedMessages() throws UnauthorisedException, ServerProblemException {
        ArrayList<Integer> out = new ArrayList<>();

        try {
            try (ResultSet r = mdbc.getUndownloadedMessages()) {
                while (r.next()) {

                    String senderUser = r.getString("SenderUsername");

                    int messageReference = r.getInt("MessageReference");
                    PrivateKeyLink[] privateKeys = getPrivateKeyWithLink();

                    String messageResult = "";

                    PenguMessengerRESTClient.Message tempMessage = restClient.getMessage(username, token, messageReference + "");

                    PenguEncryptedMessage encryptedMessage = new PenguEncryptedMessage(tempMessage.getMessage());

                    String publicKeyHash = encryptedMessage.extractPublicKeyHash();
                    int privateKeyToUse = -1;

                    //System.out.println(publicKeyHash+"Message Pk hash: ");
                    for (int i = 0; i < privateKeys.length; i++) {
                        //System.out.println(privateKeys[i].getPublicKeyHash()+"Private Key pk hash ");
                        //System.out.println(privateKeys[i].getPublicKeyHash().equalsIgnoreCase(publicKeyHash));
                        if (privateKeys[i].getPublicKeyHash().equalsIgnoreCase(publicKeyHash)) {
                            privateKeyToUse = i;
                        }
                    }

                    boolean success = false;

                    if (privateKeyToUse > -1) {
                        PenguMessage decryptedMessage = new PenguMessage(encryptedMessage.getDecryptedString(privateKeys[privateKeyToUse].getPrivateKeyString()));

                        if (decryptedMessage.isUserMessage()) {
                            messageResult = decryptedMessage.getContent()[0].getContent();
                            success = true;
                        } else {
                            //service message
                        }

                    } else {
                        success = false;
                        //messageResult = "--- Could not Decrypt this message ---";
                    }

                    if (success) {

                        boolean updatePubKey = false;

                        if (cdbc.getUserIDByUsername(senderUser) == -1) {
                            updatePubKey = true;
                        }

                        mdbc.addReceivedMessage(senderUser, messageResult, messageReference);// message refrence is marked as downloaded aswell
                        //System.out.println("downloaded");
                        try (ResultSet r2 = mdbc.getChatDetails(cdbc.getUserIDByUsername(senderUser))) {
                            r2.next();// since chat has been created already, no verification needed

                            int chatID = r2.getInt("ChatID");
                            out.add(chatID);
                        } // since chat has been created already, no verification needed

                        if (updatePubKey) {//add the senders public key so you can chat with them.
                            updateContactPublicKey(senderUser);
                        }

                        //add send delivered service message here
                    } else {
                        //System.out.println("fail");
                        mdbc.markFailedDecryptionAsDownloaded(messageReference);
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex + " @ downloadInboxedMessages");
        } catch (ServerErrorException ex) {
            throw new ServerProblemException("Server Error.");
        } catch (BadRequestException ex) {
            throw new ServerProblemException("Bad Request.");
        } catch (NotFoundException ex) {
            throw new ServerProblemException("Not Found.");
        } catch (ServiceDownException ex) {
            throw new ServerProblemException("Service Is Down.");
        } catch (BadConnectionException ex) {
            throw new ServerProblemException("Could Not Connect to Server, Check Network Connection");
        } catch (ParseException ex) {
            System.out.println(ex + "@ downloadInboxedMessages");
        }

        return out;

    }

    /**
     * Helper method that sends all messages in the out-box to the server.
     *
     * @return Returns an ArrayList of integers of the affected chats, chat IDs.
     * @throws pengu.messenger.PenguMessengerClient.ServerProblemException
     * @throws pengu.messenger.PenguRESTClient.UnauthorisedException
     * @throws pengu.messenger.PenguRESTClient.ForbiddenException
     */
    private ArrayList<Integer> sendOutboxedMessages() throws ServerProblemException, UnauthorisedException, ForbiddenException {
        ArrayList<Integer> out = new ArrayList<>();
        try {
            try (ResultSet r = mdbc.getMessagesToSend()) {
                while (r.next()) {

                    String messageContent = r.getString("MessageContent");
                    String publicKey = r.getString("PublicKeyString");
                    int OutID = r.getInt("OutID");
                    String recipient = r.getString("Recipient");

                    PenguMessage.ContentItem[] contentItem = {new PenguMessage.ContentItem(messageContent, PenguMessage.TEXT_CONTENT_TYPE)};

                    PenguMessage unencryptedMessage = new PenguMessage(PenguMessage.USER_MESSAGE_TYPE, contentItem);

                    PenguEncryptedMessage em = new PenguEncryptedMessage();

                    String encryptedMessage = em.getEncryptedString(publicKey, unencryptedMessage.getMessageString());

                    restClient.sendMessage(username, token, encryptedMessage, recipient);

                    try (ResultSet r2 = mdbc.getChatDetails(cdbc.getUserIDByUsername(recipient))) {
                        r2.next();// since chat has been created already, no verification needed

                        int chatID = r2.getInt("ChatID");
                        out.add(chatID);
                    } // since chat has been created already, no verification needed

                    mdbc.markMessageAsSent(OutID);
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex + " @ sendOutboxedMessages");
        } catch (ServerErrorException ex) {
            throw new ServerProblemException("Server Error.");
        } catch (BadRequestException ex) {
            throw new ServerProblemException("Bad Request.");
        } catch (NotFoundException ex) {
            throw new ServerProblemException("Not Found.");
        } catch (ServiceDownException ex) {
            throw new ServerProblemException("Service Is Down.");
        } catch (BadConnectionException ex) {
            throw new ServerProblemException("Could Not Connect to Server, Check Network Connection");
        } catch (ParseException ex) {
            Logger.getLogger(PenguMessengerClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return out;

    }

    /**
     * Make an edit to a specified contact's friendly name in the database.
     *
     * @param username The username of the user to edit.
     * @param friendlyName The new friendly name for the user.
     */
    public void editContactFriendlyName(String username, String friendlyName) {
        cdbc.editContactFriendlyName(username, friendlyName);
    }

    /**
     * Make an edit to a specified contact's additional information in the
     * database.
     *
     * @param username The username of the user to edit.
     * @param additionalInformation The new additional information for the user.
     */
    public void editContactAdditionalInformation(String username, String additionalInformation) {
        cdbc.editContactAdditionalInformation(username, additionalInformation);
    }

}
