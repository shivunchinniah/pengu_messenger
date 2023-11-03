package pengu.database;

/**
 * This class stores constants that store configuration data about the user database file.
 * @author Shivun
 */
public class DBSetup {
    
    /**
     * Stores the base directory of the database file
     */
    public static final String BASE_DIR = System.getProperty("user.home")+"\\AppData\\Roaming\\Pengu Messenger"+"\\DB";

    /**
     * Stores the application database file naming convention.
     */
    public static final String APP_DB = "/PenguMessengerMain.accdb";
   
    /**
     * Stores the template database file name.
     */
    public static final String APP_DB_TEMPLATE = "/PenguMessengerMain-Template.accdb";
    
    /**
     * Creates a user specific application database file name.
     * 
     * @param username The username of the user.
     * @return Returns the user specific application database file name.
     */
    public static String getAppDBForUser(String username){
        return "/"+username.toUpperCase()+"-"+APP_DB.replace("/", "");
    }
   
    /**
     * This class stores the database table names
     */
    public class Tables{

        /**
         * Stores the active chats table name.
         */
        public static final String TBL_CHATS = "tblActiveChats";

        /**
         * Stores the contacts table name.
         */
        public static final String TBL_CONTACTS = "tblContacts";

        /**
         * Stores the contact type table name.
         */
        public static final String TBL_CONTACT_TYPES = "tblContactType";

        /**
         * Stores the in-box table name.
         */
        public static final String TBL_IN_BOX = "tblInBox";

        /**
         * Stores the out-box table name.
         */
        public static final String TBL_OUT_BOX = "tblOutBox";

        /**
         * Stores the private key table name.
         */
        public static final String TBL_PRIVATE_KEYS = "tblLocalPrivateKeys";

        /**
         * Stores the public key table name.
         */
        public static final String TBL_PUBLIC_KEYS = "tblLocalPublicKeys";

        /**
         * Stores the received messages table name.
         */
        public static final String TBL_RECEIVED = "tblReceivedMessages";
            
        /**
         * Stores the sent messages table name.
         */
        public static final String TBL_SENT = "tblSentMessages";            
            
    }
    
    
}
