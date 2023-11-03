package pengu.messenger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This class is used to differ service-sent messages, from user-sent messages
 * 
 * @author Shivun Chinniah
 */
public final class PenguMessage {

    /**
     * Constant to identify a Service message.
     */
    public static final String SERVICE_MESSAGE_TYPE = "s";

    /**
     * Constant to identify a User message.
     */
    public static final String USER_MESSAGE_TYPE = "u";

    /**
     * Constant to identify content as text.
     */
    public static final String TEXT_CONTENT_TYPE = "text";

    /**
     * Constant to identify content as a link
     */
    public static final String LINK_CONTENT_TYPE = "link";

    /**
     * Constant to identify content as an image
     */
    public static final String IMAGE_CONTENT_TYPE = "image";

    /**
     * Constant to identify content as a file
     */
    public static final String FILE_CONTENT_TYPE = "file";

    /**
     * Constant to identify content as a video
     */
    public static final String VIDEO_CONTENT_TYPE = "video";

    /**
     * Allowed Images types constant array.
     */
    public static final String[] IMAGE_TYPES = {"jpg", "png", "gif", "jpeg"};

    /**
     * Allowed Video types constant array.
     */
    public static final String[] VIDEO_TYPES = {"avi", "flv", "wmv", "mov", "mp4", "wav"};
    
    /**
     * Constant to identify a message as delivered.
     */
    public static final String SERVICE_CODE_DELIVERED = "delivered";

    /**
     * Constant to identify a message's ID.
     */
    public static final String SERVICE_MESSAGE_REFERENCE_TYPE = "smessageid";

    /**
     * Constant to identify a message as read.
     */
    public static final String SERVICE_CODE_READ = "read";

    /**
     * Constant to identify a service code.
     */
    public static final String SERVICE_CODE_TYPE = "servicecode";
    
    
    /*
    Sample Service message
    {"type":"s","content":[{"c":"delivered","t":"servicecode"},{"c":"85","t":"smessageid"}]}
    
    the above string is a service message, is has a service code of: "delivered" and coressponds to message ID: 85
    
    */
    
    /**
     * Stores the message string, that contains type, content, and content type information.
     * 
     *  Message String format: (JSON String)
     *  {
     *      type:       <type>,
     *      content:    [
     *                      {c:<content1>,t: <content1 type>},
     *                      {c:<content2>,t: <content2 type>},
     *                      ...
     *                  ]
     *  }
     */
    private String messageString;

    /**
     * Stores the JSON Object of the message.
     * 
     * The JSON Object has two keys: "type", "content" The Content key has and
     * array of JSON Objects as its value, and the Type key has a String for its
     * value.
     */
    private JSONObject messageObject;

    /**
     * Parameterized constructor method, that receives an un-parsed JSON String and then creates a JSON Object.
     * 
     * @param messageString The JSON string of the message.
     */
    public PenguMessage(String messageString) {
        this.messageString = messageString;
        makeMessageObject();
    }

    /**
     * Parameterized constructor method, that receives a message type String, and an array of Content Items, then makes a JSON Object and JSON String of the message. 
     * 
     * @param type The Message type. (Service or User)
     * @param contentItems The array of Content Items of the message.
     */
    public PenguMessage(String type, ContentItem[] contentItems) {
        makeMessageObjectFromComponents(type, contentItems);
        makeMessageString();
    }

    /**
     * Makes the JSON Object from the JSON String.
     */
    private void makeMessageObject() {
        try {
            JSONParser parser = new JSONParser();
            messageObject = (JSONObject) parser.parse(messageString);
        } catch (ParseException ex) {

        }

    }
    /**
     * Makes the JSON String from the JSON Object.
     */
    private void makeMessageString() {
        messageString = messageObject.toString();
    }

    /**
     * Helper method to create the JSON Object from a String message type and an array of Content Items.
     * @param type The message type. (Service or User)
     * @param contentItems The array of Content Items of the message.
     */
    private void makeMessageObjectFromComponents(String type, ContentItem[] contentItems) {
        messageObject = new JSONObject();
        messageObject.put("type", type);
        JSONArray contentList = new JSONArray();
        for (ContentItem contentItem : contentItems) {
            JSONObject temp = new JSONObject();
            temp.put("c", contentItem.getContent());
            temp.put("t", contentItem.getLabel());
            contentList.add(temp);
        }
        messageObject.put("content", contentList);

    }

   

    /**
     * Accessor method for messageString field.
     * 
     * @return Returns the String value of the messageString field.
     */
    public String getMessageString() {
        return messageString;
    }

    /**
     * Extracts the message's type from the JSON Object.
     * 
     * @return Returns a String value of the message type. (Service or User)
     */
    public String getMessageType() {
        return (String) messageObject.get("type");
    }

    /**
     * Extracts the message's content from the JSON Object.
     * 
     * @return Returns an array of ContentItems of the content of the message.
     */
    public ContentItem[] getContent() {

        JSONArray tempContent = (JSONArray) messageObject.get("content");
        ContentItem[] out = new ContentItem[tempContent.size()];

        for (int i = 0; i < tempContent.size(); i++) {
            JSONObject tempObj = (JSONObject) tempContent.get(i);
            out[i] = new ContentItem((String) tempObj.get("c"), (String) tempObj.get("t"));
        }

        return out;

    }
    
    /**
     * Extracts the messages type, and presents a boolean value corresponding to the message's type.
     * 
     * @return Returns a boolean. TRUE - User type, FALSE - Service
     */
    public boolean isUserMessage(){
       return (getMessageType().equalsIgnoreCase(USER_MESSAGE_TYPE));
    }

    /**
     * This class is used to store a message's content, and the type of the content.
     */
    public static class ContentItem {

        private final String content;
        private final String label;

        /**
         * Parameterized constructor method, receives both the content and the type/label of the content.
         * @param content The actual content.
         * @param label The type of the content.
         */
        public ContentItem(String content, String label) {
            this.content = content;
            this.label = label;
        }

        /**
         * Accessor method for the content field.
         * @return Returns a String value of the content field.
         */
        public String getContent() {
            return content;
        }

        /**
         * Accessor method for the label field. 
         * @return Returns a String value of the label field.
         */
        public String getLabel() {
            return label;
        }

    }
    
  

}
