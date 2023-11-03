package pengusecurity.encrypt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class Allows for the generation of AES keys, encryption and decryption
 * using the AES keys, and the exporting and importing of AES keys as Base64
 * Strings
 *
 * @author Shivun Chinniah
 *
 */
public class PenguAESEncryption {

    /**
     * Stores the secret key (AES key).
     */
    private final SecretKeySpec secretKey;

    /**
     * Stores the initialization vector (IV) to be used for AES-CBC
     * encryption/decryption.
     */
    private final IvParameterSpec ivSpec;

    /**
     * Stores the AES key length constant.
     */
    public static final int KEY_SIZE = 256;

    /**
     * Default constructor method, also generates a new AES key.
     *
     * @throws NoSuchAlgorithmException
     */
    public PenguAESEncryption() throws NoSuchAlgorithmException {
        //Generate Key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(KEY_SIZE);
        secretKey = (SecretKeySpec) keyGen.generateKey();

        //Generate IV
        byte[] iv = new byte[16];
        SecureRandom sRand = new SecureRandom();
        sRand.nextBytes(iv);
        ivSpec = new IvParameterSpec(iv);

    }

    /**
     * Parameterized constructor, receives a Base64 encoded String that contains
     * the AES key and the IV separated by a '#'. E.g.
     * "SOME_BASE64_KEY_HERE#SOME_BASE64_IV_HERE"
     *
     * @param keyString The Base64 encoded AES key String
     */
    public PenguAESEncryption(String keyString) {
        String[] parts = keyString.split("#");// Separate the Secret key from the IV using the # delimiter

        secretKey = new SecretKeySpec(Base64.getDecoder().decode(parts[0]), "AES");
        ivSpec = new IvParameterSpec(Base64.getDecoder().decode(parts[1]));

    }

    /**
     * Creates an Base64 encoded String that contains the AES key and the IV
     * separated by a '#'. E.g. "SOME_BASE64_KEY_HERE#SOME_BASE64_IV_HERE"
     *
     * @return Returns a String containing the encoded AES key and IV.
     */
    public String getKeyString() {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded())
                + "#"
                + // Combine the Secret key with the IV using the # delimiter
                Base64.getEncoder().encodeToString(ivSpec.getIV());
    }

    /**
     * Helper method to encrypt data in the form of a byte array.
     *
     * @param input The byte array of the data to be encrypted.
     * @return Returns a byte array of the encrypted data.
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidParameterSpecException
     * @throws UnsupportedEncodingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    private byte[] encryptData(byte[] input) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] ciphertext = cipher.doFinal(input);
        return ciphertext;
    }

    public String decryptFile(InputStream input, String filename, String fileTypeSuffix) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        File tempFile;
        tempFile = File.createTempFile(filename, "." + fileTypeSuffix);
        FileOutputStream fileWriter = new FileOutputStream(tempFile);
        CipherOutputStream cipherOutputStream = new CipherOutputStream(fileWriter, cipher);
        byte[] buffer = new byte[2048];
        int bytesRead;
        while ((bytesRead = input.read()) >= 0) {
            cipherOutputStream.write((byte)bytesRead);
        }
        cipherOutputStream.close();
        fileWriter.close();
        return tempFile.getAbsolutePath();
    }

    /*
    BufferedOutputStream fileWriter = new BufferedOutputStream(new FileOutputStream(tempFile));
                byte[] buffer = new byte[2048];
                int bytesRead;
                while ((bytesRead = cipherInputStream.read(buffer)) >= 0) {
                    fileWriter.write(buffer, 0, bytesRead);
                }
     */
    
    public InputStream encryptFile(String filePath) throws FileNotFoundException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException{
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        FileInputStream inFile = new FileInputStream(new File(filePath));
        return new CipherInputStream(inFile, cipher);
    }
    
    /**
     * Creates a Base64 encoded and encrypted String of the inputed String data.
     *
     * @param message The input String to be encrypted.
     * @return Returns he Base64 encoded and encrypted String of the input
     * String.
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidParameterSpecException
     * @throws UnsupportedEncodingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public String encryptMessageString(String message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        return Base64.getEncoder().encodeToString(encryptData(message.getBytes()));

    }

    /**
     * Decrypts an inputed Base64 encoded and encrypted String.
     *
     * @param encryptedMessage The encrypted message String.
     * @return Returns a String containing the decrypted message.
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public String decryptMessageString(String encryptedMessage) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        return new String(decryptData(Base64.getDecoder().decode(encryptedMessage)));
    }

    /**
     * Helper method to decrypt data in the form of a byte array.
     *
     * @param encrypted The encrypted byte array.
     * @return Returns a byte array containing the data in a decrypted form.
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    private byte[] decryptData(byte[] encrypted) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        return cipher.doFinal(encrypted);

    }
    
    
    public static void main (String[] args) throws IOException{
        
        try {
            PenguAESEncryption ae = new PenguAESEncryption();//generate new key;
            
            String filePath = ("D://diorb back.jpg");
            InputStream x = ae.encryptFile(filePath);
            
            FileOutputStream fw = new FileOutputStream(new File("D://diorbenc.jpg"));
            
            byte[] buffer = new byte[2048];
            int next;
            while((next = x.read()) >= 0){
                fw.write((byte)next);
            }
            fw.close();
            x.close();
            
            FileInputStream infile = new FileInputStream(new File("D://diorbenc.jpg"));
            
            System.out.println(ae.decryptFile(infile, "myfile", "jpg"));
            
            
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(PenguAESEncryption.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            System.out.println("File not found");
        } catch (InvalidKeyException ex) {
            Logger.getLogger(PenguAESEncryption.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(PenguAESEncryption.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(PenguAESEncryption.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }

}
