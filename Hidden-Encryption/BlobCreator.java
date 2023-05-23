import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.nio.file.*;
import java.util.Random;

public class BlobCreator {
    public static void main(String[] args) throws Exception {

        // Load the key
        String keyHex = Files.readString(Paths.get("task2.key")).strip();
        byte[] keyBytes = BlobDecryptor.hexStringToByteArray(keyHex);

        // Calculate the hash of the key
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] hashedKey = md5.digest(keyBytes);

        // Load the data
        byte[] data = Files.readAllBytes(Paths.get("task2.data"));

        // Calculate the hash of the data
        byte[] dataHash = md5.digest(data);

        // Create the blob and fill with it with the right content in the right order
        byte[] blob = new byte[2*hashedKey.length + data.length + dataHash.length];
        System.arraycopy(hashedKey, 0, blob, 0, hashedKey.length);
        System.arraycopy(data, 0, blob, hashedKey.length, data.length);
        System.arraycopy(hashedKey, 0, blob, hashedKey.length + data.length, hashedKey.length);
        System.arraycopy(dataHash, 0, blob, 2*hashedKey.length + data.length, dataHash.length);

        // Initialize the cipher
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        // Encrypt the blob
        byte[] encryptedBlob = cipher.doFinal(blob);

        // Get the offset
        int offset = Integer.parseInt(Files.readString(Paths.get("task2.offset")).strip());

        // Create a container file
        byte[] container = new byte[2048];

        // Fill the container with random data
        new Random().nextBytes(container);

        // Place the blob at the offset
        System.arraycopy(encryptedBlob, 0, container, offset, encryptedBlob.length);

        // Save the container
        Files.write(Paths.get("file2.data"), container);
    }
}
