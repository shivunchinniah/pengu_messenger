package pengusecurity.encrypt;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * This class Allows for the generation of RSA (public and private) key pairs, encryption and decryption using the RSA key pairs, and the exporting and importing of RSA key pairs as Base64 Strings
 * 
 * @author Shivun Chinniah
 */
public class PenguKeyPair {
    
    /**
     * Stores the RSA private key.
     */
    private Key privateKey;
    
    /**
     * Stores the RSA public key.
     */
    private Key publicKey;
    
    /**
     * Stores the RSA key length constant.
     */
    public static final int KEY_LENGTH = 2048;
    
    /**
     * Default constructor method, also generates a new RSA key pair.
     * 
     * @throws NoSuchAlgorithmException 
     */
    public PenguKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator KeyGen = KeyPairGenerator.getInstance("RSA");
        KeyGen.initialize(KEY_LENGTH);

        KeyPair pair = KeyGen.genKeyPair();

        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();

    }
    
    /**
     * Parameterized constructor method that receives a Base64 encoded RSA private key String.
     * It is to be used when data will only be encrypted.
     * 
     * @param privateKey The Base64 encoded private key String.
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException 
     */
    public PenguKeyPair(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException{
        this.privateKey = stringToPrivateKey(privateKey);
    }
    
    /**
     * Parameterized constructor method that receives a Base64 encoded RSA private or public key String.
     * It the type of key to be used is determined by a boolean: TRUE - public key (decryption only), FALSE - private key (encryption only). 
     * 
     * @param key The Base64 encoded public or private key String.
     * @param usePublicKey The boolean indicator that specifies which key will be used. TRUE - public key (decryption only), FALSE - private key (encryption only). 
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException 
     */
    public PenguKeyPair(String key, boolean usePublicKey) throws NoSuchAlgorithmException,  InvalidKeySpecException {
        if(usePublicKey){
            this.publicKey = stringToPublicKey(key);
        }else{
            this.privateKey = stringToPrivateKey(key);
        }
    }
    
    /**
     * Parameterized constructor method that receives two Base64 encoded Strings - the public key String and the private key String.
     * It is to be used when both the encryption of data and decryption of data is required.
     * 
     * @param privateKey The Base64 encoded private key String.
     * @param publicKey The Base64 encoded public key String.
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException 
     */
    public PenguKeyPair(String privateKey, String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException{
        this.privateKey = stringToPrivateKey(privateKey);
        this.publicKey = stringToPublicKey(publicKey);
    }
    
    /**
     * Helper method to encrypt data in the form of a byte array.
     * 
     * @param message The message to be encrypted in the form of a byte array.
     * @return Returns a byte array of the encrypted data.
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException 
     */
    private  byte[] encryptMessage(byte[] message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] cipherText;

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        cipherText = cipher.doFinal(message);

        return cipherText;
    }
    
    /**
     * Encrypts an inputed message String using RSA. 
     * 
     * @param message The message String to be encrypted.
     * @return Returns a Base64 encoded and encrypted String of the message String.
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException 
     */
    public String encryptMessage(String message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
        return Base64.getEncoder().encodeToString(encryptMessage(message.getBytes()));
    }
    
    /**
     * Decrypts an inputed Base64 encoded and encrypted String using RSA.
     * 
     * @param encrypted The Base64 encoded and encrypted String.
     * @return Returns a String of the decrypted message.
     * @throws Exception 
     */
    public String decryptMessage(String encrypted) throws Exception {
        return new String(decryptMessage(Base64.getDecoder().decode(encrypted)));
    }
    
    /**
     * Helper method to decrypts data in the form of a byte array.
     * 
     * @param encrypted The byte array containing the encrypted data.
     * @return Returns a byte array of the decrypted data.
     * @throws Exception 
     */
    private byte[] decryptMessage(byte[] encrypted) throws Exception {
        byte[] decryptedText;

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        decryptedText = cipher.doFinal(encrypted);

        return decryptedText;
    }
    
    /**
     * Converts the public key into a Base64 encoded String, to be used for exporting the public key.
     * 
     * @return Returns a Base64 encoded String of the public key.
     */
    public String publicKeyToString(){
        return Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
    }
    
    /**
     * Converts the private key into a Base64 encoded String, to be used for exporting the private key.
     * 
     * @return Returns a Base64 encoded String of the private key.
     */
    public String privateKeyToString(){
        return Base64.getEncoder().encodeToString(this.privateKey.getEncoded());
    }
   
    /**
     * Helper method to retrieve a private key from a Base64 encoded String.
     * 
     * @param keyString The Base64 encoded private key String.
     * @return Returns a java.security.Key of the decoded private key.
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException 
     */
    private Key stringToPrivateKey(String keyString) throws NoSuchAlgorithmException, InvalidKeySpecException{
        
        KeyFactory kf = KeyFactory.getInstance("RSA");
        EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyString.getBytes()));
        return kf.generatePrivate(keySpec);
    }
    
    /**
     * Helper method to retrieve a public key from a Base64 encoded String.
     * 
     * @param keyString The Base64 encoded public key.
     * @return Returns a java.security.Key of the public key.
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException 
     */
    private Key stringToPublicKey(String keyString) throws NoSuchAlgorithmException, InvalidKeySpecException{
        
        KeyFactory kf = KeyFactory.getInstance("RSA");
        EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(keyString.getBytes()));
        return kf.generatePublic(keySpec);
    }

    

}
