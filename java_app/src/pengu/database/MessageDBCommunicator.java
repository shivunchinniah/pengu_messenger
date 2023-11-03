package pengu.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import pengu.database.DBSetup.Tables;

/**
 * This class provides the methods required for message related database transactions.
 * 
 * @author Shivun Chinniah
 */
public class MessageDBCommunicator extends PenguAccessDBCommunicator {

    /**
     * Parameterized constructor method.
     *
     * @param dbAddress The storage address of the database.
     */
    public MessageDBCommunicator(String dbAddress) {
        super(dbAddress);
    }

    /**
     * Creates a new Chat with the specified user.
     *
     * @param username The username of the user to which create the new chat must be created.
     * @return Returns an integer count of the number of affected rows.
     */
    public int createNewChat(String username) {
        ContactsDBCommunicator cdbc = new ContactsDBCommunicator(dbAddress);
        int userID = cdbc.getUserIDByUsername(username);
       
        if (userID > -1) {
            return queryUpdate("INSERT INTO " + Tables.TBL_CHATS + " (UserID, DateStarted, LastMessageSeen) VALUES (" + userID + ", " + PenguAccessDBCommunicator.now() + ", true)");
        } else {
            cdbc.addUser(username);
            userID = cdbc.getUserIDByUsername(username);
            return queryUpdate("INSERT INTO " + Tables.TBL_CHATS + " (UserID, DateStarted, LastMessageSeen) VALUES (" + userID + ", " + PenguAccessDBCommunicator.now() + ", true)");
        }

    }


    /**
     * Fetches the ChatID from the Active Chats database for the corresponding
     * User ID.
     *
     * @param userID The ID of the User to which the chat belongs.
     * @return Returns a ResultSet containing column "ChatID".
     */
    public ResultSet getChatDetails(int userID) {
        return query("SELECT ChatID FROM " + Tables.TBL_CHATS + " WHERE userID=" + userID);
    }

    /**
     * Fetches the ChatID form the Active Chat database for the specified user,
     * and creates a new chat if one has not been created already.
     *
     * @param username The username of the user.
     * @return Returns the ChatID as an integer.
     */
    public int getForcedChatID(String username) {
        int ChatID = getUserChatID(username);
        //System.out.println(ChatID);

        if (ChatID > -1) {

            return ChatID;
        } else {
            createNewChat(username);
            ChatID = getUserChatID(username);
            return ChatID;
        }

        /*
        try {
            ContactsDBCommunicator cdbc = new ContactsDBCommunicator(dbAddress);
            int userID = cdbc.getUserIDByUsername(username);
            ResultSet r = getChatDetails(userID);
            if(r.next()){//there is allready a chat
                return r.getInt("ChatID");
                
            }else{
                r.close();
                createNewChat(username);
                r = getChatDetails(userID);
                return r.getInt("ChatID");
                
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            return -1;
        }
         */
    }

    /**
     * Adds a sent message to the Sent Messages database and places it on the
     * Out-box table.
     *
     * @param chatID The ID of the Chat to which the message belongs.
     * @param messageContent The text of the message.
     * @param publicKeyID The ID of the public Key to be used when encrypting
     * the message.
     * @return Returns an integer count of the number of affected rows.
     */
    public int addSentMessage(int chatID, String messageContent, int publicKeyID) {

        int insertID = -1;

        Connection con = getConnectionInstance();
        try {
            PreparedStatement pstmt;

            String sql = "INSERT INTO " + Tables.TBL_SENT + " (ChatID, MessageContent, PublicKeyID, DateSent) VALUES( ?, ?, ?, " + PenguAccessDBCommunicator.now() + ")";

            pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setInt(1, chatID);
            pstmt.setString(2, messageContent);
            pstmt.setInt(3, publicKeyID);

            pstmt.executeUpdate(); // add the sent message while preventing SQL injection.

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                keys.next();

                insertID = keys.getInt(1);
            }
            pstmt.close();
            con.close();

            queryUpdate("INSERT INTO " + Tables.TBL_OUT_BOX + " (SMessageID) VALUES(" + insertID + ")");
            queryUpdate("UPDATE " + Tables.TBL_CHATS + " SET LastMessageDate=" + PenguAccessDBCommunicator.now() + " WHERE ChatID=" + chatID);

        } catch (SQLException ex) {
            System.out.println(ex);
        }
        return insertID;
    }

    /**
     * Marks the out-box record as sent.
     * 
     * @param outID The ID of the out-box record.
     * @return Returns an integer count of the number of affected rows.
     */
    public int markMessageAsSent(int outID) {

        return queryUpdate("DELETE FROM " + Tables.TBL_OUT_BOX + " WHERE OutID=" + outID);

    }

    /**
     * Fetches a list of Messages that need to be sent from the out-box table.
     *
     * @return Returns a ResultSet containing columns: "MessageContent",
     * "Recipient", "PublicKeyString", and "OutID".
     */
    public ResultSet getMessagesToSend() {
        return query("SELECT tblSentMessages.MessageContent, tblOutBox.OutID, tblLocalPublicKeys.PublicKeyString, tblContacts.Username as Recipient FROM tblContacts INNER JOIN (tblLocalPublicKeys INNER JOIN (tblSentMessages INNER JOIN tblOutBox ON tblSentMessages.SMessageID = tblOutBox.SMessageID) ON tblLocalPublicKeys.PublicKeyID = tblSentMessages.PublicKeyID) ON tblContacts.userID = tblLocalPublicKeys.userID");
    }

    /**
     * Adds a In-box/Message reference to the In-box table
     *
     * @param messageReference The integer reference received from the server.
     * @param senderUsername The String representation of the username of the
     * sender to which the reference belongs.
     * @return Returns an integer count of the number of affected rows.
     */
    public int addMessageInboxReference(int messageReference, String senderUsername) {

        if (!messageReferenceExists(messageReference)) {
            return queryUpdate("INSERT INTO " + Tables.TBL_IN_BOX + " (MessageReference, SenderUsername) VALUES (" + messageReference + ", '" + senderUsername + "')");
        } else {
            return 0;
        }

    }

    /**
     * Checks if the specified message reference exists.
     * @param messageReference The message reference of the in-box record.
     * @return Returns TRUE if the message reference exists, else FALSE.
     */
    public boolean messageReferenceExists(int messageReference) {
        ResultSet r;
        try {
            r = query("SELECT MessageReference FROM " + Tables.TBL_IN_BOX + " WHERE MessageReference=" + messageReference);
            boolean temp = r.next();
            r.close();
            return temp;
        } catch (SQLException ex) {
            System.out.println(ex + " @ messageReferenceExists");

            return true;
        }

    }

    /**
     * Fetches message references from the In-box table that have not yet been
     * used to download a message.
     *
     * @return Returns a ResultSet containing columns: "MessageReference" and
     * "SenderUsername".
     */
    public ResultSet getUndownloadedMessages() {
        return query("SELECT MessageReference, SenderUsername FROM " + Tables.TBL_IN_BOX + " WHERE Downloaded=false");
    }

    /**
     * Fetches a list of Downloaded message references to skip the process of
     * adding them.
     *
     * @return Returns a ResultSet containing columns: "MessageReference" and
     * "SenderUsername".
     */
    public ResultSet getDownloadedMessageReferences() {
        return query("SELECT MessageReference, SenderUsername FROM " + Tables.TBL_IN_BOX + " WHERE Downloaded=true");
    }

    /**
     * Deletes a Sent Message From the database.
     *
     * @param sentMessageID The ID of the Sent Message.
     * @return Returns an integer count of the number of affected rows.
     */
    public int deleteSentMessage(int sentMessageID) {
        return queryUpdate("DELETE FROM " + Tables.TBL_SENT + " WHERE SMessageID=" + sentMessageID);
    }

    /**
     * Deletes a Received Message From the database.
     *
     * @param receivedMessageID The ID of the Received Message.
     * @return Returns an integer count of the number of affected rows.
     */
    public int deleteReceivedMessage(int receivedMessageID) {
        return queryUpdate("DELETE FROM " + Tables.TBL_RECEIVED + " WHERE RMessageID=" + receivedMessageID);
    }

    /**
     * Fetches a list of the active chats from the Active Chats Database.
     *
     * @return Returns a ResultSet containing columns "Username", "DisplayName",
     * "HEXColourCode" (the contact group color code), "NamingConvention" (the
     * contact group naming convention) and LastMessageSeen.
     */
    public ResultSet getListOfChats() {
        return query("SELECT tblContacts.Username, tblContacts.DisplayName, tblContactType.NamingConvention, tblContactType.HexColourCode, tblActiveChats.ChatID, LastMessageSeen FROM (tblContactType INNER JOIN tblContacts ON tblContactType.ContactTypeID = tblContacts.ContactTypeID) INNER JOIN tblActiveChats ON tblContacts.userID = tblActiveChats.userID ORDER BY LastMessageDate DESC, DateStarted DESC");
    }

    /**
     * Fetches a list of Sent Messages from the Sent Messages database for the
     * specified chat.
     *
     * @param chatID The ID of the Chat.
     * @return Returns a ResultSet containing columns "ChatID",
     * "MessageContent", "DeliveryStatus", "ReadStatus", "DateDelivered",
     * "DateRead", "PublicKeyID", and "DateSent".
     */
    public ResultSet getSentMessagesFromChat(int chatID) {
        return query("SELECT tblSentMessages.ChatID, tblSentMessages.MessageContent, tblSentMessages.DeliveryStatus, tblSentMessages.ReadStatus, tblSentMessages.DateDelivered, tblSentMessages.DateRead, tblSentMessages.PublicKeyID, tblSentMessages.DateSent, tblSentMessages.SMessageID FROM tblSentMessages WHERE ChatID=" + chatID);
    }

    /**
     * Fetches the last sent message from a specified chat.
     * 
     * @param chatID The ID of the chat.
     * @return Returns a ResultSet with columns: "ChatID", "MessageContent", "DeliveryStatus", "ReadStatus", "DateDelivered", "DateRead", "PublicKeyID", "DateSent", and "SMessageID".
     */
    public ResultSet getLastSentMessageFromChat(int chatID) {
        return query("SELECT TOP 1 tblSentMessages.ChatID, tblSentMessages.MessageContent, tblSentMessages.DeliveryStatus, tblSentMessages.ReadStatus, tblSentMessages.DateDelivered, tblSentMessages.DateRead, tblSentMessages.PublicKeyID, tblSentMessages.DateSent, tblSentMessages.SMessageID FROM tblSentMessages WHERE ChatID=" + chatID + " ORDER BY tblSentMessages.DateSent DESC");
    }

    /**
     * Fetches a list of Received Messages from the Sent Messages database for
     * the specified chat.
     *
     * @param chatID The ID of the Chat.
     * @return Returns a ResultSet containing columns "ChatID",
     * "MessageContent", "DateReceived".
     */
    public ResultSet getReceivedMessagesFromChat(int chatID) {

        queryUpdate("UPDATE " + Tables.TBL_CHATS + " SET LastMessageSeen=true WHERE ChatID=" + chatID);
        return query("SELECT tblReceivedMessages.ChatID, tblReceivedMessages.MessageContent, tblReceivedMessages.DateReceived, tblReceivedMessages.RMessageID FROM tblReceivedMessages WHERE ChatID=" + chatID);
    }

    /**
     * Fetches the last received message from a specified chat.
     * 
     * @param chatID The ID of the chat.
     * @return Returns a ResultSet with columns: "ChatID", "MessageContent",  "PublicKeyID", "DateReceived", and "RMessageID".
     */
    public ResultSet getLastReceivedMessageFromChat(int chatID) {
        return query("SELECT TOP 1 tblReceivedMessages.ChatID, tblReceivedMessages.MessageContent, tblReceivedMessages.DateReceived, tblReceivedMessages.RMessageID FROM tblReceivedMessages WHERE ChatID=" + chatID + " ORDER BY tblReceivedMessages.DateReceived DESC");
    }

    /**
     * Deletes the specified chat from the active chats table.
     * @param chatID The ID of the chat.
     * @return Returns an integer count of the number of affected rows.
     */
    public int deleteActiveChat(int chatID) {
        return queryUpdate("DELETE FROM " + Tables.TBL_CHATS + " WHERE ChatID=" + chatID);
    }

    /**
     * Sets the status of a sent message to 'Read'.
     *
     * @param sentMessageID The ID of the sent message.
     * @param readDate The Date the message was read.
     * @return Returns an integer count of the number of affected rows.
     */
    public int setMessageRead(int sentMessageID, Date readDate) {
        DateFormat df = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");
        String formattedReadDate = df.format(readDate);
        return queryUpdate("UPDATE " + Tables.TBL_SENT + " SET ReadStatus=true, DateRead=#" + formattedReadDate + "# WHERE SMessageID=" + sentMessageID);
    }

    /**
     * Sets the status of a sent message to 'Delivered'.
     *
     * @param sentMessageID The ID of the sent message.
     * @param deliveredDate The delivery date of the send message.
     * @return Returns an integer count of the number of affected rows.
     */
    public int setMessageDelivered(int sentMessageID, Date deliveredDate) {
        DateFormat df = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");
        String formattedDeliveredDate = df.format(deliveredDate);
        return queryUpdate("UPDATE " + Tables.TBL_SENT + " SET ReadStatus=true, DateRead=#" + formattedDeliveredDate + "# WHERE SMessageID=" + sentMessageID);
    }

    /**
     * Fetches the ID of the Chat with a specified user.
     *
     * @param username The Username of the user.
     * @return Returns an integer containing the "ChatID", if no "ChatID" exists
     * with the specified user -1 is returned.
     */
    public int getUserChatID(String username) {
        ResultSet rs = query("SELECT ChatID FROM " + Tables.TBL_CHATS + " WHERE userID=(SELECT userID FROM " + Tables.TBL_CONTACTS + " WHERE Username='" + username + "')");
        try {
            if (rs.next()) {
                return rs.getInt("ChatID");
            } else {
                return -1;
            }
        } catch (SQLException ex) {
            System.out.println("Error @ getUserChatID @ MessageDBC");
            return -1;

        }
    }

    /**
     * Adds a received message into the Received Messages table and creates a
     * chat with the user if one has not been created yet. This method also
     * marks the in-box reference to downloaded so the message is not downloaded
     * again.
     *
     * @param username The username of the Message sender.
     * @param message The text of the message.
     * @param messageReference The message reference of the message.
     * @return Returns an integer - the ID of the Received message.
     */
    public int addReceivedMessage(String username, String message, int messageReference) {
        queryUpdate("UPDATE " + Tables.TBL_IN_BOX + " SET Downloaded=true WHERE MessageReference=" + messageReference);// set reference to downloaded

        queryUpdate("UPDATE " + Tables.TBL_CHATS + " SET LastMessageSeen=false, LastMessageDate=" + PenguAccessDBCommunicator.now() + " WHERE userID=(SELECT userID FROM " + Tables.TBL_CONTACTS + " WHERE Username='" + username + "')");

        int ChatID = getUserChatID(username);
        if (ChatID > -1) {

            addReceivedMessageAndGetInsertID(ChatID, message);

        } else {
            createNewChat(username);
            ChatID = getUserChatID(username);

            addReceivedMessageAndGetInsertID(ChatID, message);
        }
        return -1;
    }

    private int addReceivedMessageAndGetInsertID(int chatID, String message) {
        int key = -1;
        Connection con = getConnectionInstance();
        try {

            PreparedStatement pstmt;

            String sql = "INSERT INTO " + Tables.TBL_RECEIVED + " (ChatID, MessageContent, DateReceived) VALUES(?, ?, ?)";

            pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setInt(1, chatID);
            pstmt.setString(2, message);
            pstmt.setLong(3, PenguAccessDBCommunicator.now());

            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                keys.next();

                key = keys.getInt(1);
            }
            pstmt.close();
            con.close();

        } catch (SQLException ex) {
            System.out.println(ex + " @ queryUpdateForRecievedMessages");
        }
        return key;
    }

    /**
     * Marks a specified in-box record as Downloaded, as the private key required to decrypt it can not be found.
     * @param messageReference The message reference for the in-box record.
     * @return Returns an integer count of the number of affected rows.
     */
    public int markFailedDecryptionAsDownloaded(int messageReference) {
        return queryUpdate("UPDATE " + Tables.TBL_IN_BOX + " SET Downloaded=true WHERE MessageReference=" + messageReference);// set reference to downloaded
    }

    /*
    public int addMessageInboxReference(int messageReference, String senderUsername) {
        return queryUpdate("INSERT INTO " + Tables.TBL_IN_BOX + " (MessageReference, SenderUsername) VALUES (" + messageReference + ",'" + senderUsername + "')");
    }
     */
    /**
     * Fetches a ResultSet containing In-box/Message References that need to be
     * downloaded.
     *
     * @return Returns a ResultSet with columns: "InID", "MessageReference", and
     * "SenderUsername".
     */
    public ResultSet getMessageInboxReferences() {
        return query("SELECT InID, MessageReference, SenderUsername FROM " + Tables.TBL_IN_BOX);
    }

    /**
     * Deletes a In-box/Message Reference from the In-box table as it has been
     * downloaded from the server.
     *
     * @param inID The ID of the In-box/Message Reference
     * @return Returns an integer count of the number of affected rows.
     */
    public int deleteMessageInboxReference(int inID) {
        return queryUpdate("DELETE FROM " + Tables.TBL_IN_BOX + " WHERE InID=" + inID);
    }

}
