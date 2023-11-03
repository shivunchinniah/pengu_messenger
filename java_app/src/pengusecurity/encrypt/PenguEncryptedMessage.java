package pengusecurity.encrypt;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import pengu.messenger.PenguMessengerClient;


/**
 * This class combines the AES encryption and RSA encryption classes to encrypt and decrypt messages of varying sizes.
 * This class creates and breaks apart "encrypted message Strings".
 * 
 * @author hkrmn
 * @see PenguAESEncryption
 * @see PenguKeyPair
 */
public class PenguEncryptedMessage {
    
    /**
     * Stores the delimiter constant, that is used to separate  a RSA public key hash, a RSA encrypted AES key, and a AES encrypted Message.
     * Format: SOME_RSA_PUBLIC_KEY_HASH#SOME_RSA_ENCRYPTED_AES_KEY#SOME_AES_ENCRYPTED_MESSAGE
     */
    public static final String DELIMITER = "#";
    
    /**
     * Stores the encrypted message String.
     */
    private String encryptedMessage;
    
    /**
     * Stores the public key hash.
     */
    private String publicKeyHash;
    
   
    
    /**
     * Creates a SHA1Hash that is encoded in hexadecimal form.
     * 
     * @param input The input String to be hashed.
     * @return Returns a SHA1 hexadecimal  hashed String.
     */
    public static String getSHA1Hash(String input){
        return PenguMessengerClient.getSHA1Hash(input).replace(" ", "");
    }
    
    /**
     * Default constructor method.
     */
    public PenguEncryptedMessage(){
        
    }
    
    /**
     * Parameterized constructor method that receives an encrypted message string.
     * 
     * @param encryptedMessage The encrypted message String. Format: SOME_RSA_PUBLIC_KEY_HASH#SOME_RSA_ENCRYPTED_AES_KEY#SOME_AES_ENCRYPTED_MESSAGE.
     */
    public PenguEncryptedMessage(String encryptedMessage){
        this.encryptedMessage = encryptedMessage;
        // Extract the public key hash
        try (Scanner scMessage = new Scanner(this.encryptedMessage).useDelimiter(DELIMITER)) {
            this.publicKeyHash = scMessage.next();
            scMessage.close();
        }
        
    }
    
    /**
     * Creates an encrypted message String using an inputed public key and a message.
     * 
     * @param publicKey The Base64 encoded public key string to be used for encrypting the AES key.
     * @param unencryptedMessage The message String.
     * @return Returns an encrypted message String. Format: SOME_RSA_PUBLIC_KEY_HASH#SOME_RSA_ENCRYPTED_AES_KEY#SOME_AES_ENCRYPTED_MESSAGE.
     */
    public String getEncryptedString(String publicKey, String unencryptedMessage) {

        try {
            String RSAPublicKeyHash;
            String EncryptedAESKey;
            String EncryptedMessage;

            RSAPublicKeyHash = getSHA1Hash(publicKey);// Create public key hash

            PenguAESEncryption ae = new PenguAESEncryption(); //generate a AES key
            PenguKeyPair kp = new PenguKeyPair(publicKey, true);
            EncryptedAESKey = kp.encryptMessage(ae.getKeyString()); //encrypt the generated AES key
            EncryptedMessage = ae.encryptMessageString(unencryptedMessage);// encrypt the message using the AES key

            return RSAPublicKeyHash + DELIMITER + EncryptedAESKey + DELIMITER + EncryptedMessage; // Combine the public key hash, the encrypted AES key, and the encrypted message

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidParameterSpecException | UnsupportedEncodingException | InvalidAlgorithmParameterException ex) {
            System.out.println(ex + "@getEncryptedString");
        }
        
        return "";// failed

    }
    
    /**
     * Extracts the encrypted message portion of an encrypted message String, and decrypts it using an inputed Base64 encoded private key String.
     * 
     * @param privateKey The Base64 encoded private key String.
     * @return Returns a decrypted message String.
     */
    public String getDecryptedString(String privateKey) {
        try {
            
            String[] parts = encryptedMessage.split(DELIMITER);
            
            
            PenguKeyPair kp = new PenguKeyPair(privateKey);

            String AESKey = kp.decryptMessage(parts[1]);
            
            
            PenguAESEncryption ae = new PenguAESEncryption(AESKey);
            
            return ae.decryptMessageString(parts[2]);
                    
        } catch (NoSuchAlgorithmException ex) {
            System.out.println(ex + "@getDecryptedString");
        } catch (InvalidKeySpecException ex) {
            System.out.println(ex + "@getDecryptedString");
        } catch (Exception ex) {
            System.out.println(ex + "@getDecryptedString");
        }
        
        return "";

    }
    
    /**
     * A method to retrieve the extracted public key hash. 
     * 
     * @return Returns a String with the value of the publicKeyHash field.
     */
    public String extractPublicKeyHash() {
        return publicKeyHash;
    }
    

}
