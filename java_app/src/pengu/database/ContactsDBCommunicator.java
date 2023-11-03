/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pengu.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import pengu.database.DBSetup.Tables;

/**
 * This class provides the methods required for contact related database transactions.
 * 
 * @author Shivun Chinniah
 */
public class ContactsDBCommunicator extends PenguAccessDBCommunicator {

    /**
     * Parameterized constructor method
     * 
     * @param dbAddress the address of the database file.
     */
    public ContactsDBCommunicator(String dbAddress) {
        super(dbAddress);
    }

    /**
     * Fetches a list of user details: UserID, Username, DisplayName,
     * ContactTypeID, NamingConvention (of the contact type), HexColourCode (of
     * the contact type).
     *
     * @return Returns a ResultSet
     * @see java.sql.ResultSet
     */
    public ResultSet fetchUsers() {
        return query("SELECT UserID, Username, DisplayName, " + Tables.TBL_CONTACTS + ".ContactTypeID, NamingConvention,  HexColourCode  FROM " + Tables.TBL_CONTACTS + " LEFT JOIN " + Tables.TBL_CONTACT_TYPES + " ON " + Tables.TBL_CONTACT_TYPES + ".ContactTypeID = " + Tables.TBL_CONTACTS + ".ContactTypeID ORDER BY Username, DisplayName");

    }

    /**
     * Adds a user to the contacts list table. Only the user's Username is added
     * and other fields are set to default.
     *
     * @param username The String representation of the Username
     * @return Returns an integer describing how many records where affected.
     */
    public int addUser(String username) {
        return queryUpdate("INSERT INTO " + Tables.TBL_CONTACTS + " (Username) VALUES (\"" + username + "\")");
    }

    /**
     * Fetches the userID of a user from a specified username.
     *
     * @param username They user's username string.
     * @return Returns an Integer representation of the userID, if the user does
     * not exist -1 will be returned.
     */
    public int getUserIDByUsername(String username) {
        ResultSet rs = query("SELECT UserID FROM " + Tables.TBL_CONTACTS + " WHERE Username='" + username + "' ");
        try {
            if (rs.next()) {
                return rs.getInt("UserID");
            } else {
                return -1;
            }
        } catch (SQLException ex) {
            return -1;
        }
    }

    /**
     * Fetches the specified user's contact details from the contact related tables.
     * 
     * @param username The username of the user.
     * @return  Returns a ResultSet containing columns: "UserID", "Username", "DisplayName", "AdditionalInformation", "NamingConvention", and "HEXColorCode".
     */
    public ResultSet getContactDetails(String username) {
        return query("SELECT UserID, Username, DisplayName, AdditionalInformation, NamingConvention, HEXColourCode FROM " + Tables.TBL_CONTACTS + " INNER JOIN " + Tables.TBL_CONTACT_TYPES + " ON tblContacts.contactTypeID = tblContactType.contactTypeID WHERE Username='" + username + "'");
    }

    /**
     * Edits the user's details in the contact list table.
     *
     * @param userID The user's ID
     * @param displayName The user's display name, a friendly name.
     * @param contactTypeID The contact type to which the user should remain/be
     * added.
     * @param additiionalInformation Any additional information about the user.
     * @return Returns an integer describing how many records where affected.
     */
    public int editContact(int userID, String displayName, int contactTypeID, String additiionalInformation) {
        return queryUpdate("UPDATE " + Tables.TBL_CONTACTS + " SET DisplayName=\"" + displayName + "\", ContactTypeID=" + contactTypeID + ", AdditionalInformation=\"" + additiionalInformation + "\" WHERE UserID=" + userID);
    }

    /**
     * Edits the user's friendly name in the database.
     * 
     * @param username The user's username.
     * @param friendlyName The new friendly name for the user.
     */
    public void editContactFriendlyName(String username, String friendlyName) {
        try {
            PreparedStatement pstmt;
            try (Connection con = this.getConnectionInstance()) {
                pstmt = con.prepareStatement("UPDATE " + Tables.TBL_CONTACTS + " SET Displayname=? WHERE Username='" + username + "'");
                pstmt.setString(1, friendlyName);
                pstmt.executeUpdate();
            }
            pstmt.close();

        } catch (SQLException ex) {
            System.out.println(ex + " @ editContactFriendlyName");
        }

    }

    /**
     * Edits the user's additional information in the database.
     * @param username The user's username.
     * @param additionalInformation The new additional information for the user.
     */
    public void editContactAdditionalInformation(String username, String additionalInformation) {
        try {
            PreparedStatement pstmt;
            try (Connection con = this.getConnectionInstance()) {
                pstmt = con.prepareStatement("UPDATE " + Tables.TBL_CONTACTS + " SET AdditionalInformation=? WHERE Username='" + username + "'");
                pstmt.setString(1, additionalInformation);
                pstmt.executeUpdate();
            }
            pstmt.close();

        } catch (SQLException ex) {
            System.out.println(ex + " @ editContactAdditionalInformation");
        }
    }

    /**
     * Deletes the user from the contact list table.
     *
     * @param userID The user's ID.
     * @return Returns an integer describing how many records where affected.
     */
    public int deleteContact(int userID) {
        return queryUpdate("DELETE FROM " + Tables.TBL_CONTACTS + " WHERE UserID=" + userID);
    }

    /**
     * Adds a contact type to the contact type table.
     *
     * @param namingConvention The friendly name or naming convention for the
     * new contact group.
     * @param hexColourCode The colour code (HEX) of the contact group.
     * @return Returns an integer describing how many records where affected.
     */
    public int addContactType(String namingConvention, String hexColourCode) {
        return queryUpdate("INSERT INTO " + Tables.TBL_CONTACT_TYPES + " (NamingConvention, HexColourCode) VALUES(\"" + namingConvention + "\", \"" + hexColourCode + "\")");
    }

    /*
    
    
     */
    /**
     * Edits the details of a specified contact type.
     *
     * @param ContactTypeID The contact group's ID.
     * @param namingConvention The new naming convention of the contact group.
     * @param hexColourCode The new colour code (HEX) of the contact group.
     * @return Returns an integer describing how many records where affected.
     */
    /*public int editContactType(int ContactTypeID, String namingConvention, String hexColourCode){
        return queryUpdate("UPDATE "+Tables.TBL_CONTACT_TYPES+" SET NamingConvention='"+namingConvention+"', HexColourCode='"+hexColourCode+"' WHERE ContactTypeID="+ContactTypeID);
    }
    
     */
    /**
     * Fetches a list of Contact Types from the database.
     *
     * @return Returns a ResultSet containing columns: "ContactTypeID",
     * "NamingConvention", and "HexColourCode".
     */
    public ResultSet getContactTypes() {
        return query("SELECT ContactTypeID, NamingConvention, HexColourCode FROM " + Tables.TBL_CONTACT_TYPES);
    }

    /**
     * Edits the contact type of a contact.
     *
     * @param username The username of the contact.
     * @param contactTypeID The contact type ID to set.
     * @return Returns an integer count of number of affected rows.
     */
    public int setContactContactType(String username, int contactTypeID) {
        return queryUpdate("UPDATE " + Tables.TBL_CONTACTS + " SET ContactTypeID=" + contactTypeID + " WHERE Username='" + username + "'");
    }

    //Helper method for deleteContactType method. Changes all the users who belong to a contact group to the defualt user group
    private int changeContactTypeTo(int from, int to) {
        return queryUpdate("UPDATE " + Tables.TBL_CONTACTS + " SET ContactTypeID=" + to + " WHERE ContactTypeID=" + from);
    }

    /**
     * Deletes the specified contact group.
     *
     * @param contactTypeID The contact group's ID.
     * @return Returns an integer describing how many records where affected.
     */
    public int deleteContactType(int contactTypeID) {
        changeContactTypeTo(contactTypeID, 1);
        return queryUpdate("DELETE FROM " + Tables.TBL_CONTACT_TYPES + " WHERE ContactTypeID=" + contactTypeID);
    }

    /**
     * Fetches a list of Public Keys for a specified user.
     *
     * @param userID The user's ID.
     * @return Returns a ResultSet containing columns: "PublicKeyString",
     * "PublicKeyExpiryDate", and "PublicKeyRegistrationDate".
     * @see java.sql.ResultSet
     */
    public ResultSet getPublicKeys(int userID) {
        return query("SELECT PublicKeyID, PublicKeyString, PublicKeyExpiryDate, PublicKeyRegistrationDate FROM " + Tables.TBL_PUBLIC_KEYS + " WHERE UserID=" + userID + " ORDER BY PublicKeyExpiryDate DESC");
    }

    /**
     * Adds a Public Key for a specified user.
     *
     * @param userID The user's ID.
     * @param publicKeyString The String (Base64) representation of the Public
     * Key.
     * @param regDate The date when the Public Key was created.
     * @param expDate The date when the Public Key expires.
     * @return Returns an integer describing how many records where affected.
     */
    public int addPublicKey(int userID, String publicKeyString, Date regDate, Date expDate) {

        Calendar temp = Calendar.getInstance();

        temp.setTime(regDate);
        long regDateFormatted = temp.getTimeInMillis();

        temp.setTime(expDate);
        long expDateFormatted = temp.getTimeInMillis();

        return queryUpdate("INSERT INTO " + Tables.TBL_PUBLIC_KEYS + " (UserID, PublicKeyRegistrationDate, PublicKeyExpiryDate, PublicKeyString) VALUES(" + userID + ", " + regDateFormatted + " , " + expDateFormatted + ", \"" + publicKeyString + "\")");
    }

    /**
     * Adds a Private Key to the local Private Key table.
     *
     * @param privateKeyString The String (Base64) representation of the Private
     * Key.
     * @param regDate The date when the Private Key was created.
     * @param expDate The date when the Private Key expires.
     * @param publicKeyHash The Public Key hash String.
     * @return Returns an integer describing how many records where affected.
     */
    public int addPrivateKey(String privateKeyString, Date regDate, Date expDate, String publicKeyHash) {

        Calendar temp = Calendar.getInstance();

        temp.setTime(regDate);
        long regDateFormatted = temp.getTimeInMillis();

        temp.setTime(expDate);
        long expDateFormatted = temp.getTimeInMillis();

        return queryUpdate("INSERT INTO " + Tables.TBL_PRIVATE_KEYS + " (PrivateKeyRegistrationDate, PrivateKeyExpiryDate, PrivateKeyString, PublicKeyString) VALUES(" + regDateFormatted + " , " + expDateFormatted + ", \"" + privateKeyString + "\", '" + publicKeyHash + "')");
    }

    /**
     * Fetches a list of Private Keys from the local Private Key table.
     *
     * @return Returns a ResultSet containing columns: "PrivateKeyString",
     * "PublicKeyString", "PrivateKeyExpiryDate", and
     * "PrivateKeyRegistrationDate".
     * @see java.sql.ResultSet
     */
    public ResultSet getPrivateKeys() {
        return query("SELECT PrivateKeyString, PrivateKeyExpiryDate, PrivateKeyRegistrationDate, PublicKeyString FROM " + Tables.TBL_PRIVATE_KEYS + " ORDER BY PrivateKeyExpiryDate");
    }

    

}
