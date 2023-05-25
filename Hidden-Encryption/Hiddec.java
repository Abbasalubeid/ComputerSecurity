import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.MessageDigest;
import java.nio.file.*;
import java.util.Arrays;

public class Hiddec {
    public static void main(String[] args) throws Exception {
        // Parse arguments
        String keyHex = null;
        String ctrHex = null;
        Path input = null;
        Path output = null;

        for (String arg : args) {
            if (arg.startsWith("--key=")) {
                keyHex = arg.substring(6);
            } else if (arg.startsWith("--ctr=")) {
                ctrHex = arg.substring(6);
            } else if (arg.startsWith("--input=")) {
                input = Paths.get(arg.substring(8));
            } else if (arg.startsWith("--output=")) {
                output = Paths.get(arg.substring(9));
            }
        }

        // Check arguments
        if (keyHex == null || input == null || output == null) {
            System.out.println("Missing required argument.");
            System.exit(1);
        }

        // Convert hexadecimal strings to byte arrays
        byte[] keyBytes = hexStringToByteArray(keyHex);

        // Calculate the hash of the key
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] hashedKey = md5.digest(keyBytes);

        // Load the data file
        byte[] inputBytes;
        try {
            inputBytes = Files.readAllBytes(input);
        } catch (Exception e) {
            System.out.println("Unable to read input file.");
            System.exit(1);
        }

        // Initialize the cipher
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher;
        if (ctrHex != null) {
            cipher = Cipher.getInstance("AES/CTR/NoPadding");
            byte[] ctrBytes = hexStringToByteArray(ctrHex);
            IvParameterSpec ivSpec = new IvParameterSpec(ctrBytes);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] keyWithAES = cipher.doFinal(hashedKey);

            // Find where the blob starts and make it the start of the input byte array
            for (int i = 0; i <= inputBytes.length - keyWithAES.length; i++) {
                byte[] slice = Arrays.copyOfRange(inputBytes, i, i + keyWithAES.length);
                if (Arrays.equals(slice, keyWithAES)) {
                    inputBytes = Arrays.copyOfRange(inputBytes, i, i + inputBytes.length);
                    break;
                }
                if (i == inputBytes.length - keyWithAES.length) {
                    System.out.println("No matching blob found in the input.");
                    System.exit(1);
                    return;
                }
            }

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        } else {
            cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
        }

        // Decrypt the data file
        byte[] decryptedFile;
        try {
            decryptedFile = cipher.doFinal(inputBytes);
        } catch (Exception e) {
            System.out.println("Decryption failed.");
            System.exit(1);
            return;
        }

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
            System.out.println("Could not find the hidden data in the decrypted file.");
            System.exit(1);
            return;
        }

        // Extract the data
        byte[] data = Arrays.copyOfRange(decryptedFile, dataStart + hashedKey.length, dataEnd);

        // Extract the hash of the data
        byte[] extractedDataHash = Arrays.copyOfRange(decryptedFile, dataEnd + hashedKey.length, dataEnd + 2 * hashedKey.length);

        // Calculate the hash of the blob
        byte[] calculatedDataHash = md5.digest(data);

        // Compare the two hashes
        if (Arrays.equals(extractedDataHash, calculatedDataHash)) {
            Files.write(output, data);
        } else {
            System.out.println("Data verification failed. The data is not valid, H´' != H(Data)");
            System.exit(1);
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }
}
