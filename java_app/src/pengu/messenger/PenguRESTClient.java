package pengu.messenger;

import org.json.simple.parser.ParseException;

/**
 * This class is used when wanting to communicate with any Pengu API server That
 * uses a REST architectural style.
 *
 * @author Shivun Chinniah
 */
public class PenguRESTClient {

    /**
     * Identifies OK (General Purposes) Code.
     */
    public static final int OK_GENERAL = 200;

    /**
     * Identifies Accepted Code.
     */
    public static final int ACCEPTED = 202;

    /**
     * Identifies OK (No additional content) Code.
     */
    public static final int OK_NO_CONTENT = 204;

    /**
     * Identifies Bad request Code.
     */
    public static final int BAD_REQUEST = 400;

    /**
     * Identifies Unauthorized Code.
     */
    public static final int UNAUTHORISED = 401;

    /**
     * Identifies Forbidden Code.
     */
    public static final int FORBIDDEN = 403;

    /**
     * Identifies Not found Code.
     */
    public static final int NOT_FOUND = 404;

    /**
     * Identifies Server error Code.
     */
    public static final int SERVER_ERROR = 500;

    /**
     * Identifies service unavailable Code.
     */
    public static final int SERVICE_UNAVAILABLE = 503;
    
   
    
    /**
     * Fetches the code String of an integer code for a REST request.
     * @param code The integer code.
     * @return Returns a code String.
     */
    public static String getStatusByCode(int code) {
        switch (code) {
            case 200:
                return "OK";
            case 202:
                return "ACCEPTED";
            case 204:
                return "OK NO CONTENT";
            case 400:
                return "BAD REQUEST";
            case 401:
                return "UNAUTHORISED";
            case 403:
                return "FORBIDDEN";
            case 404:
                return "NOT FOUND";
            case 500:
                return "SERVER ERROR";
            case 503:
                return "SERVICE UNAVAILABLE";
        }
        return ""; // invalid code
    }
    
    /**
     * Stores the API server's Address and access protocol.
     */
    private String APIServerAddressAndProtocol;
    
    /**
     * Stores the API server's main directory.
     */
    private String mainDirectory;
    
    /**
     * Stores the API server's endpoint.
     */
    private String endpoint;
    
    /**
     * Parameterized constructor method, that receives the API Server's Address and access protocol (HTTP/HTTPS).
     * 
     * @param APIServerAddressAndProtocol The API Server's Address and access protocol.
     */
    PenguRESTClient(String APIServerAddressAndProtocol) {
        this.APIServerAddressAndProtocol = APIServerAddressAndProtocol;
    }

    /**
     * An exception for Bad requests.
     */
    public static class BadRequestException extends Exception {
    }

    /**
     * An exception for Unauthorized responses.
     */
    public static class UnauthorisedException extends Exception {
    }

    /**
     * An exception for BadConnections.
     */
    public static class BadConnectionException extends Exception {
    }

    /**
     * An exception for Forbidden responses.
     */
    public static class ForbiddenException extends Exception {

        private final String forbiddenMessage;

        /**
         * Parameterized constructor method to set the forbidden message.
         * @param message The forbidden message String.
         */
        public ForbiddenException(String message) {
            this.forbiddenMessage = message;
        }

        /**
         * Accessor method for the forbiddenMessage field.
         * @return Returns the String value of the forbiddenMessage field.
         */
        public String getForbiddenMessage() {
            return forbiddenMessage;
        }

    }

    /**
     * An exception for Not found responses.
     */
    public static class NotFoundException extends Exception {
    }

    /**
     * An exception for Server error responses.
     */
    public static class ServerErrorException extends Exception {
    }

    /**
     * An exception for Service down responses.
     */
    public static class ServiceDownException extends Exception {
    }
    
    

    /**
     * Parameterized constructor method that receives the API server's: address, access protocol, main directory, and endpoint.
     * @param APIServerAddressAndProtocol The API server's address and access protocol (HTTP/HTTPS).
     * @param mainDirectory The API server's main directory.
     * @param endpoint The API server's endpoint.
     */
    public PenguRESTClient(String APIServerAddressAndProtocol, String mainDirectory, String endpoint) {
        this.APIServerAddressAndProtocol = APIServerAddressAndProtocol;
        this.mainDirectory = mainDirectory;
        this.endpoint = endpoint;
    }

    /**
     * Mutator method for the APIServerAddressAndProtocol field.
     * @param APIServerAddressAndProtocol The new value of the APIServerAddressAndProtocol field.
     */
    public void setAPIServerAddressAndProtocol(String APIServerAddressAndProtocol) {
        this.APIServerAddressAndProtocol = APIServerAddressAndProtocol;
    }

    /**
     * Mutator method for the endpoint field.
     * 
     * @param endpoint The new value of the endpoint field.
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Mutator method for the mainDirectory field.
     * 
     * @param mainDirectory The new value of the mainDirectory field.
     */
    public void setMainDirectory(String mainDirectory) {
        this.mainDirectory = mainDirectory;
    }

    /**
     * Combined Mutator method for the mainDirectory and endpoint fields.
     * 
     * @param mainDirectory The new value of the mainDirectory field.
     * @param endpoint The new value of the endpoint field.
     */
    public void setMainDirectoryAndEndpoint(String mainDirectory, String endpoint) {
        this.mainDirectory = mainDirectory;
        this.endpoint = endpoint;
    }

    /**
     * Makes a request for data from the API server, using the previously set values for the API server's: address, access protocol, main directory, and endpoint.
     * 
     * @param parameters The POST parameters to send to the server. Format: {{"parameter1", "value1"},{"parameter2},{"value2}, ...}
     * 
     * @return Returns the response text form the server.
     * 
     * @throws ServerErrorException
     * @throws BadRequestException
     * @throws UnauthorisedException
     * @throws ForbiddenException
     * @throws NotFoundException
     * @throws ServiceDownException
     * @throws BadConnectionException
     */
    public String makeRequestForData(String[][] parameters) throws ServerErrorException, BadRequestException, UnauthorisedException, ForbiddenException, NotFoundException, ServiceDownException, BadConnectionException {

        APICommunicator.Request request = new APICommunicator().sendRequest(APIServerAddressAndProtocol + "/" + mainDirectory + "/" + endpoint, parameters);
        if (request.isSuccessful()) {

            try {
                int code = Integer.parseInt(APICommunicator.parseJSON(request.getReceived(), "code"));

                switch (code) {
                    case OK_GENERAL:
                        return APICommunicator.parseJSON(request.getReceived(), "data");

                    case ACCEPTED:
                        return getStatusByCode(ACCEPTED);

                    case OK_NO_CONTENT:
                        return getStatusByCode(OK_NO_CONTENT);

                    case BAD_REQUEST:
                        throw new BadRequestException();

                    case UNAUTHORISED:
                        throw new UnauthorisedException();

                    case FORBIDDEN:
                        throw new ForbiddenException(APICommunicator.parseJSON(APICommunicator.parseJSON(request.getReceived(), "data"), "message"));

                    case NOT_FOUND:
                        throw new NotFoundException();

                    case SERVER_ERROR:

                        throw new ServerErrorException();

                    case SERVICE_UNAVAILABLE:
                        throw new ServiceDownException();
                }
                throw new ServerErrorException();
            } catch (ParseException ex) {
                throw new ServerErrorException();
            }

        } else {
            switch (request.getConnectionStatus()) {
                case APICommunicator.FAILED_TO_CONNECT:
                    throw new BadConnectionException();
                case APICommunicator.BLANK_RESPONSE:
                case APICommunicator.INVALID_URL:
                case APICommunicator.SSL_CERTIFICATE_NOT_VERIFIED:
                    throw new ServerErrorException();

            }
            throw new ServerErrorException();
        }

    }

}
