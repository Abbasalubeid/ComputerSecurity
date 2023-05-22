import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.nio.file.*;
import java.util.Arrays;

public class BlobDecryptor {
    public static void main(String[] args) throws Exception {
        // Load the key
        String keyHex = Files.readString(Paths.get("task1.key")).strip();
        byte[] keyBytes = hexStringToByteArray(keyHex);

        // Calculate the hash of the key
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] hashedKey = md5.digest(keyBytes);

        // Load the data file
        byte[] dataFile = Files.readAllBytes(Paths.get("task1.data"));

        // Initialize the cipher
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        // Decrypt the data file
        byte[] decryptedFile = cipher.doFinal(dataFile);

        // Search for the hidden data
        int dataStart = -1;
        int dataEnd = -1;
        for (int i = 0; i <= decryptedFile.length - hashedKey.length; i++) {
            byte[] slice = Arrays.copyOfRange(decryptedFile, i, i + hashedKey.length);
            if (Arrays.equals(slice, hashedKey)) {
                if (dataStart == -1) {
                    dataStart = i;
                } else {
                    dataEnd = i;
                    break;
                }
            }
        }
        if (dataStart == -1 || dataEnd == -1) {
            System.out.println("Could not find the hidden data in the data file.");
        }

      // Extract the data
      byte[] data = Arrays.copyOfRange(decryptedFile, dataStart + hashedKey.length, dataEnd);

      // Extract the hash of the data
      byte[] extractedDataHash = Arrays.copyOfRange(decryptedFile, dataEnd + hashedKey.length, dataEnd + 2 * hashedKey.length);

      // Calculate the hash of the blob
      byte[] calculatedDataHash = md5.digest(data);

      // Compare the two hashes
      if (Arrays.equals(extractedDataHash, calculatedDataHash)) {
        Files.write(Paths.get("file1.data"), data);
      } else {
          System.out.println("Data verification failed. The data is not valid, H´' != H(Data)");
          return;
      }
     
    }

    private static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
          int index = i * 2;
          int v = Integer.parseInt(s.substring(index, index + 2), 16);
          b[i] = (byte) v;
        }
        return b;
    }
}
