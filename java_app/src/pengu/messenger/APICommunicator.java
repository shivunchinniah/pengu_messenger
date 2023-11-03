package pengu.messenger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.net.ssl.HttpsURLConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This class is used to make HTTP requests with an API server.
 * 
 * @author Shivun Chinniah
 */
public class APICommunicator {

    /**
     * Success - Constant String.
     */
    public final static String SUCCESS = "Ok";
    /**
     *  Blank response - Constant String.
     */
    public final static String BLANK_RESPONSE = "Nothing received ";
    /**
     * Time out - Constant String.
     */
    public final static String TIME_OUT = "Server timeout";
    /**
     * Failed to connect - Constant String.
     */
    public final static String FAILED_TO_CONNECT = "Failed to connect to the server";
    /**
     * Invalid URS - Constant String.
     */
    public final static String INVALID_URL = "Inalid URL";
    /**
     *  SSL certificate not verified - Constant String.
     */
    public final static String SSL_CERTIFICATE_NOT_VERIFIED = "The certificate provided by the server is not verified, or can not be verified. DO NOT TRUST SERVER";
    /**
     * Connection time out - Constant integer.
     */
    public final static int CONNECTION_TIMEOUT_MLS = 14000;
    /**
     * Read time out - Constant integer.
     */
    public final static int READ_TIMEOUT_MLS = 14000;

    /**
     * Stores the Connection instance to the server.
     */
    private HttpURLConnection con;

    /**
     * Default Constructor
     */
    public APICommunicator() {
        // Nothing needs to be set
    }

    /**
     * Used to send a HTTP request to the Pengu API Server.
     *
     * @param URL The complete URL of the API server (The server's:
     * protocol://address/main directory/endpoint).
     * @param parameters The HTTP POST parameters. A sample input:
     * {{"username","pengu"},{"password","123456"}}.
     * @return Returns a APICommunicator.Request Object
     * @see APICommunicator.Request
     */
    public Request sendRequest(String URL, String[][] parameters) {
        String received = "";
        Request output = new Request("", BLANK_RESPONSE, false, "");
        try {
            URL url = new URL(URL);

            if (url.getProtocol().equalsIgnoreCase("HTTP")) {
                con = (HttpURLConnection) url.openConnection();
            } else {
                con = (HttpsURLConnection) url.openConnection();
            }

            con.setRequestMethod("POST");
            con.setChunkedStreamingMode(8 * 1024);
            con.setConnectTimeout(CONNECTION_TIMEOUT_MLS);
            con.setReadTimeout(READ_TIMEOUT_MLS);

            Map<String, String> sendParameters = new HashMap<>();

            for (String[] parameter : parameters) {
                sendParameters.put(parameter[0], parameter[1]);
            }

            con.setDoOutput(true);
            try (DataOutputStream out = new DataOutputStream(con.getOutputStream())) {
                out.writeBytes(getParamsString(sendParameters));
                out.flush();
            }

            Scanner screceived = new Scanner(con.getInputStream());
            if (screceived.hasNext()) {

                received = screceived.nextLine();
                output.setConnectionStatus(SUCCESS);
                output.setSuccess(true);

            }
            while (screceived.hasNext()) {
                received += "\n" + screceived.nextLine();
            }

            output.setreceived(received);
            output.setContentType(con.getContentType());

        } catch (MalformedURLException ex) {
            output.setConnectionStatus(INVALID_URL);
            output.setSuccess(false);
            output.setreceived(ex + "");
            con.disconnect();

        } catch (javax.net.ssl.SSLHandshakeException ex) {

            output.setConnectionStatus(SSL_CERTIFICATE_NOT_VERIFIED);
            output.setSuccess(false);
            output.setreceived(ex + "");
            con.disconnect();

        } catch (IOException ex) {
            output.setConnectionStatus(FAILED_TO_CONNECT);
            output.setSuccess(false);
            output.setreceived(ex + "");
            con.disconnect();
        }

        con.disconnect();
        return output;
    }

    /**
     * Helper method to create an HTTP POST body with the specified parameters and values.
     * 
     * @param params The parameter map of parameters and values.
     * @return Returns a String that can be used in an HTTP POST body, that also specifies parameters and values.
     * @throws UnsupportedEncodingException 
     */
    private static String getParamsString(Map<String, String> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }

    /**
     * A Request object is returned when a request is made to the API server.
     */
    public static class Request {

        private String received, connectionStatus, contentType;
        private boolean success;

        /**
         * Parameterized constructor method for a Request object.
         *
         * @param received The String representation of what is received from
         * the API server. It is the body of the HTTP response.
         * @param connectionStatus The String representation of a connection
         * status, it could be an error status or a success status.
         * @param success A Boolean representation of the connection. TRUE means
         * successful, FALSE means that an error occurred.
         * @param contentType The String representation of a HTTP MIME Type.
         * E.g. application/json or image/png.
         */
        public Request(String received, String connectionStatus, boolean success, String contentType) {
            this.received = received;
            this.connectionStatus = connectionStatus;
            this.success = success;
            this.contentType = contentType;
        }

        /**
         * Accessor method for received field.
         *
         * @return Returns current value of the received field.
         */
        public String getReceived() {
            return received;
        }

        /**
         * Mutator method for received field.
         *
         * @param received sets value of corresponding field to this parameter.
         */
        public void setreceived(String received) {
            this.received = received;
        }

        /**
         * Mutator method for connectionStatus field.
         *
         * @param connectionStatus sets value of corresponding field to this
         * parameter.
         */
        public void setConnectionStatus(String connectionStatus) {
            this.connectionStatus = connectionStatus;
        }

        /**
         * Mutator method for connectionStatus field.
         *
         * @param success sets value of corresponding field to this parameter.
         */
        public void setSuccess(boolean success) {
            this.success = success;
        }

        /**
         * Accessor method for connectionStatus field.
         *
         * @return Returns current value of the connectionStatus field.
         */
        public String getConnectionStatus() {
            return connectionStatus;
        }

        /**
         * Indicates if connection is successful.
         *
         * @return Returns TRUE if successful, else false.
         */
        public boolean isSuccessful() {
            return success;
        }

        /**
         * Mutator method for connectionStatus field.
         *
         * @param contentType sets value of corresponding field to this
         * parameter.
         */
        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        /**
         * Accessor method for contentType field.
         *
         * @return Returns current value of the contentType field.
         */
        public String getContentType() {
            return contentType;
        }

    }

    /**
     * Parses JSON text, allows for value at a specified key to be retrieved.
     *
     * @param jsonText String containing JSON text.
     * @param key The key field that indicates what value needs to be retrieved
     * from the JSON text.
     * @return Returns a String representation containing the value retrieved
     * from the JSON text
     * @throws ParseException
     */
    public static String parseJSON(String jsonText, String key) throws ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(jsonText);
        JSONObject jsonObject = (JSONObject) obj;
        return jsonObject.get(key) + "";
    }

    /**
     * Parses a JSOn array, by converting each item in the array to a String
     * representation.
     *
     * @param jsonText String containing JSON text.
     * @return Returns a String array with each item in the JSON array.
     * @throws ParseException
     */
    public static String[] parseJSONArray(String jsonText) throws ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(jsonText);
        JSONArray jsonArr = (JSONArray) obj;
        String[] out = new String[jsonArr.size()];
        int i = 0;
        for (Object temp : jsonArr) {
            out[i] = temp + "";
            i++;
        }

        return out;
    }
}
