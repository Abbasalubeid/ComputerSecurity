import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.MessageDigest;
import java.nio.file.*;
import java.util.Random;

public class Hidenc {
    public static void main(String[] args) throws Exception {
        // Parse arguments
        String keyHex = null;
        String ctrHex = null;
        Path input = null;
        Path output = null;
        Path template = null;
        int size = -1;
        int offset = -1;

        for (String arg : args) {
            if (arg.startsWith("--key=")) {
                keyHex = arg.substring(6);
            } else if (arg.startsWith("--ctr=")) {
                ctrHex = arg.substring(6);
            } else if (arg.startsWith("--input=")) {
                input = Paths.get(arg.substring(8));
            } else if (arg.startsWith("--output=")) {
                output = Paths.get(arg.substring(9));
            } else if (arg.startsWith("--template=")) {
                template = Paths.get(arg.substring(11));
            } else if (arg.startsWith("--size=")) {
                size = Integer.parseInt(arg.substring(7));
            } else if (arg.startsWith("--offset=")) {
                offset = Integer.parseInt(arg.substring(9));
            }
        }

        // Check arguments
        if (keyHex == null || input == null || output == null) {
            System.out.println("Missing required argument.");
            System.exit(1);
        }

        // Check length of key and ctr
        if (keyHex.length() != 32 || (ctrHex != null && ctrHex.length() != 32)) {
            System.out.println("Key and CTR must be 32 hexadecimal digits (16 bytes).");
            System.exit(1);
        }

        // Check size
        if (size != -1 && (size <= 0 || size % 16 != 0)) {
            System.out.println("Size must be a positive multiple of 16.");
            System.exit(1);
        }

        // Check that only one of --template or --size is specified
        if (template != null && size != -1) {
            System.out.println("Either --template or --size must be specified, not both.");
            System.exit(1);
        }

        // Convert hexadecimal strings to byte arrays
        byte[] keyBytes = hexStringToByteArray(keyHex);

        // Calculate the hash of the key
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] hashedKey = md5.digest(keyBytes);

        // Load the data
        byte[] data = null;
        try {
            data = Files.readAllBytes(input);
        } catch (Exception e) {
            System.out.println("Error reading input file: " + e.getMessage());
            System.exit(1);
        }

        // Calculate the hash of the data
        byte[] dataHash = md5.digest(data);

        // Check if the blob will fit in the container
        if (size != -1 && size < 2*hashedKey.length + data.length + dataHash.length) {
            System.out.println("Size is not big enough to contain the blob.");
            System.exit(1);
        }

        // Create the blob and fill it with the right content in the right order
        byte[] blob = new byte[2*hashedKey.length + data.length + dataHash.length];
        System.arraycopy(hashedKey, 0, blob, 0, hashedKey.length);
        System.arraycopy(data, 0, blob, hashedKey.length, data.length);
        System.arraycopy(hashedKey, 0, blob, hashedKey.length + data.length, hashedKey.length);
        System.arraycopy(dataHash, 0, blob, 2*hashedKey.length + data.length, dataHash.length);

        // Initialize the cipher
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = null;
        if (ctrHex != null) {
            IvParameterSpec ivSpec = new IvParameterSpec(hexStringToByteArray(ctrHex));
            cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        } else {
            cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        }

        // Encrypt the blob
        byte[] encryptedBlob = cipher.doFinal(blob);

        // Create a container file
        byte[] container = null;
        if (template != null) {
            try {
                container = Files.readAllBytes(template);
            } catch (Exception e) {
                System.out.println("Error reading template file: " + e.getMessage());
                System.exit(1);
            }
        } else if (size != -1) {
            container = new byte[size];
            new Random().nextBytes(container);
        } else {
            System.out.println("Either --template or --size must be specified.");
            System.exit(1);
        }

        if (offset == -1) {
            offset = new Random().nextInt(container.length - encryptedBlob.length);
        }

        // Place the blob at the offset
        System.arraycopy(encryptedBlob, 0, container, offset, encryptedBlob.length);

        // Save the container
        try {
            Files.write(output, container);
        } catch (Exception e) {
            System.out.println("Error writing output file: " + e.getMessage());
            System.exit(1);
        }
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                  + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
