package pengu.messenger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class provides the methods necessary for communicating with the Pengu Messenger (PENGU) API Server.
 * 
 * @author Shivun Chinniah
 */
public class PenguMessengerRESTClient {
    

    
    /**
     * API Structure:
     * 
     * API_SERVER
     *    MAIN_DIRECTORY
     *          END_POINT
     * 
     * E.g. 'HTTP_PROTOCOL://API_SERVER/MAIN_DIRECTORY/END_POINT'
     * 
     */
    
    //API Server
    /**
     *
     */
    public static final String API_SERVER = "https://pengumessenger.ddns.net/PenguMessengerServerSide";
    
    
    //API MAIN-DIRECTORIES

    /**
     * The authenticator main directory.
     */
    public static final String AUTHENTICATOR = "authenticator.php";

    /**
     * The public key main directory.
     */
    public static final String PUBLIC_KEY = "public_key.php";

    /**
     * The private key main directory.
     */
    public static final String PRIVATE_KEY = "private_key_manager.php";

    /**
     * The message main directory.
     */
    public static final String MESSAGE = "message_transfer.php";

    //AUTHENTICATOR ENDPOINTS

    /**
     * The authenticator endpoint to request for a token.
     */
    public static final String AUTHENTICATOR_LOGIN = "get_token";

    /**
     * The authenticator endpoint to request to register.
     */
    public static final String AUTHENTICATOR_SIGNUP = "register_user";

    /**
     * The authenticator endpoint to request for email verification.
     */
    public static final String AUTHENTICATOR_EMAIL_VERIFICATION = "verify_email";

    //PUBLIC KEY ENDPOINTS

    /**
     * The public key endpoint to request for user public keys.
     */
    public static final String PUBLIC_KEY_SEARCH_USERS = "search_users";

    /**
     * The public key endpoint to request to add a public key.
     */
    public static final String PUBLIC_KEY_ADD = "add_public_key";

    //PRIVATE KEY ENDPOINTS

    /**
     * The private key endpoint to request to add a private key.
     */
    public static final String PRIVATE_KEY_ADD = "add_private_key";

    /**
     * The private key endpoint to request for private keys.
     */
    public static final String PRIVATE_KEY_GET = "get_private_keys";

    /**
     * The private key endpoint to request for a recovery key.
     */
    public static final String PRIVATE_KEY_REQUEST_RECOVERY_KEY = "request_recovery_key";

    //MESSAGE ENDPOINTS

    /**
     * The message endpoint to request to send a message parent.
     */
    public static final String MESSAGE_SEND_PARENT = "send_message_parent";

    /**
     * The message endpoint to request to send a message child.
     */
    public static final String MESSAGE_SEND_CHILD = "send_message_child";

    /**
     * The message endpoint to request for a list of inbound messages.
     */
    public static final String MESSAGE_GET_SUMMARY = "message_summary";

    /**
     * The message endpoint to request for a message parent part and its child parts.
     */
    public static final String MESSAGE_GET_PARTS = "get_message_parts";
    
    /**
     * Stores the PenguRESTClient instance for the Pengu Messenger API. 
     */
    private final PenguRESTClient clientCommunicator;

    /**
     * Default constructor method.
     */
    public PenguMessengerRESTClient() {
        clientCommunicator = new PenguRESTClient(API_SERVER);
    }
    
    
    
    //Okay and Forbidden

    /**
     * Makes a request to the server for a access token (Logging-in).
     *  
     * @param username The username of the account.
     * @param password The password of the account.
     * @return Returns a token String if authentication was successful (as JSON Response text).
     * 
     * @throws PenguRESTClient.ServerErrorException
     * @throws PenguRESTClient.BadRequestException
     * @throws PenguRESTClient.ForbiddenException
     * @throws PenguRESTClient.NotFoundException
     * @throws PenguRESTClient.ServiceDownException
     * @throws PenguRESTClient.BadConnectionException
     */
    public String getToken(String username, String password) throws PenguRESTClient.ServerErrorException, PenguRESTClient.BadRequestException, PenguRESTClient.ForbiddenException, PenguRESTClient.NotFoundException, PenguRESTClient.ServiceDownException, PenguRESTClient.BadConnectionException{
        String temp = "";

        String[][] parameters = {{"username", username}, {"password", password}};
        clientCommunicator.setMainDirectoryAndEndpoint(AUTHENTICATOR, AUTHENTICATOR_LOGIN);

        try {
            temp = APICommunicator.parseJSON(clientCommunicator.makeRequestForData(parameters), "token");
        } catch (PenguRESTClient.UnauthorisedException | org.json.simple.parser.ParseException ex) {
            // nothing should happen
        }
        // nothing should happen

        return temp;
    }

    //Accepted and Forbidden

    /**
     * Makes a request to register an account.
     * 
     * @param username The username of the account.
     * @param password The password of the account.
     * @param email The email of the account.
     * 
     * @throws PenguRESTClient.ServerErrorException
     * @throws PenguRESTClient.BadRequestException
     * @throws PenguRESTClient.ForbiddenException
     * @throws PenguRESTClient.NotFoundException
     * @throws PenguRESTClient.ServiceDownException
     * @throws PenguRESTClient.BadConnectionException
     */
    public void registerUser(String username, String password, String email) throws PenguRESTClient.ServerErrorException, PenguRESTClient.BadRequestException, PenguRESTClient.ForbiddenException, PenguRESTClient.NotFoundException, PenguRESTClient.ServiceDownException, PenguRESTClient.BadConnectionException {
        try {
            String[][] parameters = {{"username", username}, {"password", password}, {"email", email}};
            clientCommunicator.setMainDirectoryAndEndpoint(AUTHENTICATOR, AUTHENTICATOR_SIGNUP);
            clientCommunicator.makeRequestForData(parameters);
        } catch (PenguRESTClient.UnauthorisedException ex) {
            // nothing should happen
        }

    }

    //Public Key

    /**
     * This class allows for a public key object to be created to link a public key String to a username, expiry date and registration date.
     */
    public static class PublicKey {
        
        /**
         * Stores the public key String.
         */
        private final String publicKey;
        
        /**
         * Stores the username String.
         */
        private final String username;
        
        /**
         * Stores the registration date of the public key.
         */
        private final Date registrationDate;
        
        /**
         * Stores the expiry date of the public key.
         */
        private final Date expiryDate;

        /**
         * Parameterized constructor method, that receives a public key, a username and expiry and registration dates of a public key.
         * 
         * @param publicKey The public key String.
         * @param username The username String.
         * @param registrationDate The public key registration date.
         * @param expriryDate The public key expiry date.
         * @throws ParseException
         */
        public PublicKey(String publicKey, String username, String registrationDate, String expriryDate) throws ParseException {
            this.publicKey = publicKey;
            this.username = username;

            this.registrationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(registrationDate + " UTC");
            this.expiryDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").parse(expriryDate + " UTC");

        }

        /**
         * Accessor method for the expiryDate field.
         * @return Returns Date value of the expiryDate field.
         */
        public Date getExpiryDate() {
            return expiryDate;
        }

        /**
         * Accessor method for the publicKey field.
         * @return Returns String value of the publicKey field.
         */
        public String getPublicKey() {
            return publicKey;
        }

        /**
         * Accessor method for the registrationDate field.        
         * @return Returns a Date value of the registrationDate field.
         */
        public Date getRegistrationDate() {
            return registrationDate;
        }

        /**
         * Accessor method for the username field.
         * @return Returns String value of the username field.
         */
        public String getUsername() {
            return username;
        }

    }
    
    //Okay and No Content

    /**
     * Makes a request for user public keys, specified by a username (Search term), and a page number (If there are many results).
     * 
     * @param username The username (Search term).
     * @param page The page number (If there are many results).
     * @return Returns Returns JSON response text of the search results.
     * @throws PenguRESTClient.ServerErrorException
     * @throws PenguRESTClient.BadRequestException
     * @throws PenguRESTClient.UnauthorisedException
     * @throws PenguRESTClient.NotFoundException
     * @throws PenguRESTClient.ServiceDownException
     * @throws PenguRESTClient.BadConnectionException
     * @throws org.json.simple.parser.ParseException
     * @throws ParseException
     */
    public PublicKey[] searchUsers(String username, int page) throws PenguRESTClient.ServerErrorException, PenguRESTClient.BadRequestException, PenguRESTClient.UnauthorisedException, PenguRESTClient.NotFoundException, PenguRESTClient.ServiceDownException, PenguRESTClient.BadConnectionException, org.json.simple.parser.ParseException, ParseException {

        try {
            String[][] parameters = {{"username", username}, {"page", page + ""}};
            clientCommunicator.setMainDirectoryAndEndpoint(PUBLIC_KEY, PUBLIC_KEY_SEARCH_USERS);
            
            String response = clientCommunicator.makeRequestForData(parameters);
            PublicKey[] out;
            
            //if no results becuase no users found with specified username
            if (response.equals(PenguRESTClient.getStatusByCode(PenguRESTClient.OK_NO_CONTENT))) {
                out = new PublicKey[0];
            } else {
                
                String[] temp;
                temp = APICommunicator.parseJSONArray(APICommunicator.parseJSON(response, "results"));
                
                out = new PublicKey[temp.length];
                for (int i = 0; i < temp.length; i++) {
                    String json = temp[i];
                    String publicKey = APICommunicator.parseJSON(json, "public_key");
                    String user = APICommunicator.parseJSON(json, "username");
                    String registrationDate = APICommunicator.parseJSON(json, "registration_date");
                    String expiryDate = APICommunicator.parseJSON(json, "expiry_date");
                    out[i] = new PublicKey(publicKey, user, registrationDate, expiryDate);
                }
            }
            
            return out;
        } catch (PenguRESTClient.ForbiddenException ex) {
            //nothing should happen
            return null;
        }
      

    }
    
    //Okay no content and Unauthorised

    /**
     * Makes a request to add a public key to the server.
     * 
     * @param username The account username.
     * @param token The account temporary access token.
     * @param publicKey The public key String to add.
     * @throws PenguRESTClient.ServerErrorException
     * @throws PenguRESTClient.BadRequestException
     * @throws PenguRESTClient.UnauthorisedException
     * @throws PenguRESTClient.NotFoundException
     * @throws PenguRESTClient.ServiceDownException
     * @throws PenguRESTClient.BadConnectionException
     */
    public void addPublicKey(String username, String token, String publicKey) throws PenguRESTClient.ServerErrorException, PenguRESTClient.BadRequestException, PenguRESTClient.UnauthorisedException, PenguRESTClient.NotFoundException, PenguRESTClient.ServiceDownException, PenguRESTClient.BadConnectionException {
        try {
            String[][] parameters = {{"username", username}, {"token", token}, {"public_key", publicKey}};
            clientCommunicator.setMainDirectoryAndEndpoint(PUBLIC_KEY, PUBLIC_KEY_ADD);
            clientCommunicator.makeRequestForData(parameters);
        } catch (PenguRESTClient.ForbiddenException ex) {
            //nothing should happen
        }
    }

    
    //Message
    //okay, Forbide, unauthorised

    /**
     * Makes a request to add a Message to the server, creates the parent message, and calculates and creates child messages.
     * 
     * @param username The account username.
     * @param token The account temporary access token. 
     * @param message The Message String.
     * @param recipient The recipient's username.
     * @throws PenguRESTClient.ServerErrorException
     * @throws PenguRESTClient.BadRequestException
     * @throws PenguRESTClient.UnauthorisedException
     * @throws PenguRESTClient.ForbiddenException
     * @throws PenguRESTClient.NotFoundException
     * @throws PenguRESTClient.ServiceDownException
     * @throws PenguRESTClient.BadConnectionException
     * @throws org.json.simple.parser.ParseException
     */
    public void sendMessage(String username, String token, String message, String recipient) throws PenguRESTClient.ServerErrorException, PenguRESTClient.BadRequestException, PenguRESTClient.UnauthorisedException, PenguRESTClient.ForbiddenException, PenguRESTClient.NotFoundException, PenguRESTClient.ServiceDownException, PenguRESTClient.BadConnectionException, org.json.simple.parser.ParseException {
        int partsLen = message.length() / 256;

        if (message.length() % 256 != 0) {
            partsLen++;
        }

        String[] parts = new String[partsLen];

        for (int i = 0; i < partsLen; i++) {

            if (i * 256 + 256 <= message.length()) {
                parts[i] = message.substring(i * 256, (i * 256) + 256);
            } else {
                parts[i] = message.substring(i * 256);
            }

        }

        String[][] parameters = {{"username", username}, {"token", token}, {"message", parts[0]}, {"parts", partsLen + ""}, {"recipient", recipient}};
        clientCommunicator.setMainDirectoryAndEndpoint(MESSAGE, MESSAGE_SEND_PARENT);
        String temp = clientCommunicator.makeRequestForData(parameters);
        String reference = APICommunicator.parseJSON(temp, "message_reference");
        
        //System.out.println(reference);
        //System.out.println(partsLen);
        clientCommunicator.setMainDirectoryAndEndpoint(MESSAGE, MESSAGE_SEND_CHILD);
        for (int i = 1; i < partsLen; i++) {
            String[][] parametersChild = {{"username", username}, {"token", token}, {"message", parts[i]}, {"part_number", (i+1) + ""}, {"message_reference", reference}};
            //System.out.println(i+1);
            
            //System.out.println(parts[i].length());
            
            clientCommunicator.makeRequestForData(parametersChild);
        }

    }

    /**
     *  This class allows for MessageReference objects to be created, it links a message reference to a sender and size.
     */
    public static class MessageReference {

        /**
         * Stores the message reference String.
         */
        protected int messageReference;

        /**
         * Stores the message size integer.
         */
        protected int messageSize;

        /**
         * Stores the sender String.
         */
        protected String sender;
        
        /**
         * Parameterized constructor method, receives a message reference, message size, and sender.
         * @param messageReference the message reference integer of the message.
         * @param messageSize the size of the message.
         * @param sender the sender username of the message.
         */
        public MessageReference(String messageReference, String messageSize, String sender) {
            this.messageReference = Integer.parseInt(messageReference);
            this.messageSize = Integer.parseInt(messageSize);
            this.sender = sender;
        }

        /**
         * Accessor method for the messageReference field.
         * 
         * @return Returns integer value of messageReference field.
         */
        public int getMessageReference() {
            return messageReference;
        }

        /**
         * Accessor method for the messageSize field.
         * 
         * @return Returns integer value of the messageSize field.
         */
        public int getMessageSize() {
            return messageSize;
        }

        /**
         * Accessor method for the sender field.
         * 
         * @return Returns String value of sender field.
         */
        public String getSender() {
            return sender;
        }

        
        
        

    }
    
    //okay, no content and Unauthorised 

    /**
     * Makes a request to the server for inbound message (Fetches inbound message references).
     * 
     * @param username The account username.
     * @param token The account temporary access token. 
     * @return Returns JSON response text containing message references.
     * @throws PenguRESTClient.ServerErrorException
     * @throws PenguRESTClient.BadRequestException
     * @throws PenguRESTClient.UnauthorisedException
     * @throws PenguRESTClient.NotFoundException
     * @throws PenguRESTClient.ServiceDownException
     * @throws PenguRESTClient.BadConnectionException
     * @throws org.json.simple.parser.ParseException
     */
    public MessageReference[] getListOfMessageReferences(String username, String token) throws PenguRESTClient.ServerErrorException, PenguRESTClient.BadRequestException, PenguRESTClient.UnauthorisedException, PenguRESTClient.NotFoundException, PenguRESTClient.ServiceDownException, PenguRESTClient.BadConnectionException, org.json.simple.parser.ParseException {
        try {
            String[][] parameters = {{"username", username}, {"token", token}};
            clientCommunicator.setMainDirectoryAndEndpoint(MESSAGE, MESSAGE_GET_SUMMARY);
            MessageReference out[];
            String raw = clientCommunicator.makeRequestForData(parameters);
            
            
            
            //if no data because no messages
            if (raw.equalsIgnoreCase(PenguRESTClient.getStatusByCode(PenguRESTClient.OK_NO_CONTENT))) {
                out = new MessageReference[0];
            } else {
                String response = APICommunicator.parseJSON(raw, "summary");
                String[] temp;
                temp = APICommunicator.parseJSONArray(response);
                
                out = new MessageReference[temp.length];
                for (int i = 0; i < temp.length; i++) {
                    String json = temp[i];
                    String messageReference = APICommunicator.parseJSON(json, "message_reference");
                    String messageSize = APICommunicator.parseJSON(json, "message_size");
                    String sender = APICommunicator.parseJSON(json, "username");
                    out[i] = new MessageReference(messageReference, messageSize, sender);
                }
            }
            
            return out;
        } catch (PenguRESTClient.ForbiddenException ex) {
            return null;
        }
    }

    /**
     * This class extend the MessageReference class by adding a field for message content.
     */
    public static class Message extends MessageReference {

        /**
         * Stores the message content.
         */
        private final String message;

        /**
         * Parameterized constructor method, receives a message content, message reference, message size, and sender.
         * 
         * @param message The message content String.
         * @param messageReference The message reference.
         * @param messageSize The message size.
         * @param sender The sender username of the message.
         */
        public Message(String message, String messageReference, String messageSize, String sender) {
            super(messageReference, messageSize , sender);
            
            this.message = message;
        }

        /**
         * Accessor method for the message field.
         * @return Returns String value of the message field.
         */
        public String getMessage() {
            return message;
        }

    }

    //okay , unauthorised

    /**
     * Makes a request to download a specified message from the server. 
     * 
     * @param username The account username.
     * @param token The account temporary access token. 
     * @param messageReference The specified reference of the message.
     * @return Returns JSON response text of the message.
     * @throws PenguRESTClient.ServerErrorException
     * @throws PenguRESTClient.BadRequestException
     * @throws PenguRESTClient.UnauthorisedException
     * @throws PenguRESTClient.NotFoundException
     * @throws PenguRESTClient.ServiceDownException
     * @throws PenguRESTClient.BadConnectionException
     * @throws org.json.simple.parser.ParseException
     */
    public Message getMessage(String username, String token, String messageReference) throws PenguRESTClient.ServerErrorException, PenguRESTClient.BadRequestException, PenguRESTClient.UnauthorisedException, PenguRESTClient.NotFoundException, PenguRESTClient.ServiceDownException, PenguRESTClient.BadConnectionException, org.json.simple.parser.ParseException {
        try {
            String[][] parameters = {{"username", username}, {"token", token}, {"message_reference", messageReference}};
            
            String message = "";
            
            clientCommunicator.setMainDirectoryAndEndpoint(MESSAGE, MESSAGE_GET_PARTS);
            String response = clientCommunicator.makeRequestForData(parameters);
            String messageSize = APICommunicator.parseJSON(response, "message_size");
            String[] messagePartsRaw = APICommunicator.parseJSONArray(APICommunicator.parseJSON(response, "message_parts"));
            String sender = APICommunicator.parseJSON(response, "sender");
            
            
            for (String messagePartsRaw1 : messagePartsRaw) {
                message += APICommunicator.parseJSON(messagePartsRaw1, "content");
            }
            
            return new Message(message, messageReference, messageSize, sender);
        } catch (PenguRESTClient.ForbiddenException ex) {
            return null;
        }

    }

  

}
