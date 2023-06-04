import javax.crypto.*;
import javax.crypto.spec.*;
import java.math.BigInteger;
import java.util.*;


public class DoubleAesModes {

    private static final String AES = "AES";
    private static final String AES_ECB_NO_PADDING = "AES/ECB/NoPadding";
    private static final int BLOCK_SIZE = 16;  // AES block size is 16 bytes.

    private final SecretKeySpec key1;
    private final SecretKeySpec key2;

    public DoubleAesModes(byte[] key1Bytes, byte[] key2Bytes) {
        this.key1 = new SecretKeySpec(key1Bytes, AES);
        this.key2 = new SecretKeySpec(key2Bytes, AES);
    }

    // Bitwise XOR on two byte arrays.
    private byte[] xor(byte[] a, byte[] b) {
        byte[] result = new byte[Math.min(a.length, b.length)];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }

    private byte[] aesEncrypt(byte[] input, SecretKeySpec key) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ECB_NO_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        // Encrypt the input data using the cipher and return the result.
        return cipher.doFinal(input);
    }

    private byte[] aesDecrypt(byte[] input, SecretKeySpec key) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ECB_NO_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, key);

        // Decrypt the input data using the cipher and return the result.
        return cipher.doFinal(input);
    }

    private byte[] doubleAesEncrypt(byte[] input) throws Exception {
        // Perform AES encryption twice, using the two keys, and return the result.
        return aesEncrypt(aesEncrypt(input, key1), key2);
    }

    private byte[] doubleAesDecrypt(byte[] input) throws Exception {
        // Perform AES decryption twice, using the two keys, and return the result.
        return aesDecrypt(aesDecrypt(input, key2), key1);
    }

    // PKCS#7 padding to a byte array.
    private byte[] pad(byte[] input) {
        // Determine how many padding bytes are needed.
        int padLength = BLOCK_SIZE - (input.length % BLOCK_SIZE);

        // Create a new array that is a copy of the input array, but with extra space at the end for the padding.
        byte[] output = Arrays.copyOf(input, input.length + padLength);

        // Fill the extra space in the output array with the padding bytes.
        Arrays.fill(output, input.length, output.length, (byte) padLength);

        return output;
    }

    // Remove PKCS#7 padding from a byte array.
    private byte[] unpad(byte[] input) {
        // Determine how many padding bytes there are.
        int padLength = input[input.length - 1];

        // Return a new array that is a copy of the input array, but without the padding.
        return Arrays.copyOf(input, input.length - padLength);
    }

    public byte[] cbcEncrypt(byte[] plaintext, byte[] iv) throws Exception {
        // Apply PKCS#7 padding to the plaintext.
        byte[] paddedPlaintext = pad(plaintext);

        byte[] previousBlock = iv;

        // Create an array to hold the ciphertext.
        byte[] ciphertext = new byte[paddedPlaintext.length];

        // For each block of plaintext,
        for (int i = 0; i < paddedPlaintext.length; i += BLOCK_SIZE) {
            // Take the current block of plaintext.
            byte[] plaintextBlock = Arrays.copyOfRange(paddedPlaintext, i, i + BLOCK_SIZE);

            // XOR the plaintext block with the previous ciphertext block.
            byte[] xorResult = xor(plaintextBlock, previousBlock);

            // Encrypt the XOR result using double AES.
            byte[] ciphertextBlock = doubleAesEncrypt(xorResult);

            // Add the ciphertext block to the ciphertext array.
            System.arraycopy(ciphertextBlock, 0, ciphertext, i, BLOCK_SIZE);

            // Update the "previous ciphertext block" for the next iteration.
            previousBlock = ciphertextBlock;
        }

        // Return the final ciphertext.
        return ciphertext;
    }

    public byte[] cbcDecrypt(byte[] ciphertext, byte[] iv) throws Exception {
        byte[] previousBlock = iv;
        byte[] plaintext = new byte[ciphertext.length];

        for (int i = 0; i < ciphertext.length; i += BLOCK_SIZE) {
            byte[] ciphertextBlock = Arrays.copyOfRange(ciphertext, i, i + BLOCK_SIZE);

            byte[] decryptedBlock = doubleAesDecrypt(ciphertextBlock);

            byte[] plaintextBlock = xor(decryptedBlock, previousBlock);

            System.arraycopy(plaintextBlock, 0, plaintext, i, BLOCK_SIZE);

            // Update the "previous ciphertext block" for the next iteration.
            previousBlock = ciphertextBlock;
        }

        // Remove the padding from the plaintext and return the result.
        return unpad(plaintext);
    }

    // A general function for both encryption and decryption in Cipher Feedback (CFB) mode.
    public byte[] cfbMode(byte[] input, byte[] iv, boolean isEncrypt) throws Exception {
        // Prepare an array for the output (either ciphertext or plaintext).
        byte[] output = new byte[input.length];

        // Initialize the shift register with the Initialization Vector (IV).
        byte[] shiftRegister = iv;

        // Process each byte of the input (either plaintext for encryption, or ciphertext for decryption).
        for (int i = 0; i < input.length; i++) {
            // Encrypt the current contents of the shift register using double AES.
            byte[] encryptedBlock = doubleAesEncrypt(shiftRegister);

            // XOR the first byte of the encrypted block with the current input byte, creating the current output byte.
            byte outputByte = (byte) (encryptedBlock[0] ^ input[i]);

            // Store the current output byte in the output array.
            output[i] = outputByte;

            // Update the shift register: discard the first (oldest) byte and append the newly created byte if encrypting
            // or the current input byte if decrypting.
            shiftRegister = Arrays.copyOfRange(shiftRegister, 1, shiftRegister.length);
            shiftRegister = appendByte(shiftRegister, isEncrypt ? outputByte : input[i]);
        }

        // Return the output (either ciphertext or plaintext).
        return output;
    }

    // Function to perform encryption in Cipher Feedback (CFB) mode.
    public byte[] cfbEncrypt(byte[] plaintext, byte[] iv) throws Exception {
        // Padding the plaintext to make its length a multiple of the block size.
        byte[] paddedPlaintext = pad(plaintext);

        // Return the resulting ciphertext.
        return cfbMode(paddedPlaintext, iv, true);
    }

    public byte[] cfbDecrypt(byte[] ciphertext, byte[] iv) throws Exception {
        byte[] plaintext = cfbMode(ciphertext, iv, false);

        // Remove the padding from the decrypted plaintext.
        return unpad(plaintext);
    }

    // A helper function to append a byte to a byte array.
    public byte[] appendByte(byte[] array, byte b) {
        byte[] newArray = new byte[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[newArray.length - 1] = b;
        return newArray;
    }       
    
    public byte[] ctrEncrypt(byte[] plaintext, byte[] iv) throws Exception {
        // Initialize a counter with the value of the IV.
        BigInteger counter = new BigInteger(1, iv);

        // Create an array to hold the ciphertext.
        byte[] ciphertext = new byte[plaintext.length];
    
        // For each block of plaintext,
        for (int i = 0; i < plaintext.length; i += BLOCK_SIZE) {
            // Convert the counter to a byte array.
            byte[] counterBytes = counter.toByteArray();

            // Create a block of bytes from the counter.
            byte[] counterBlock = new byte[BLOCK_SIZE];
            System.arraycopy(counterBytes, 0, counterBlock, BLOCK_SIZE - counterBytes.length, counterBytes.length);

            // Encrypt the counter block using double AES.
            byte[] encryptedCounter = doubleAesEncrypt(counterBlock);
    
            // Take the current block of plaintext.
            byte[] plaintextBlock = Arrays.copyOfRange(plaintext, i, Math.min(i + BLOCK_SIZE, plaintext.length));

            // XOR the encrypted counter with the plaintext block.
            byte[] ciphertextBlock = xor(encryptedCounter, plaintextBlock);

            // Add the ciphertext block to the ciphertext array.
            System.arraycopy(ciphertextBlock, 0, ciphertext, i, plaintextBlock.length);
    
            // Increment the counter for the next iteration.
            counter = counter.add(BigInteger.ONE);
        }
    
        // Return the final ciphertext.
        return ciphertext;
    }

    public byte[] ctrDecrypt(byte[] ciphertext, byte[] iv) throws Exception {
        // Initialization of CTR mode decryption is identical to encryption.
        return ctrEncrypt(ciphertext, iv);
    }
    public static void main(String[] args) throws Exception {
        byte[] key1 = "0123456789abcdef".getBytes(); // 16 bytes
        byte[] key2 = "fedcba9876543210".getBytes(); // 16 bytes
        byte[] iv = "abcdef0123456789".getBytes(); // 16 bytes
        DoubleAesModes doubleAesModes = new DoubleAesModes(key1, key2);

        String plaintext = "This is a test message.";
        byte[] plaintextBytes = plaintext.getBytes();

        byte[] ciphertextCbc = doubleAesModes.cbcEncrypt(plaintextBytes, iv);
        System.out.println("CBC ciphertext: " + Base64.getEncoder().encodeToString(ciphertextCbc));
        byte[] decryptedCbc = doubleAesModes.cbcDecrypt(ciphertextCbc, iv);
        System.out.println("CBC decrypted: " + new String(decryptedCbc));

        byte[] ciphertextCfb = doubleAesModes.cfbEncrypt(plaintextBytes, iv);
        System.out.println("CFB ciphertext: " + Base64.getEncoder().encodeToString(ciphertextCfb));
        byte[] decryptedCfb = doubleAesModes.cfbDecrypt(ciphertextCfb, iv);
        System.out.println("CFB decrypted: " + new String(decryptedCfb));

        byte[] ciphertextCtr = doubleAesModes.ctrEncrypt(plaintextBytes, iv);
        System.out.println("CTR ciphertext: " + Base64.getEncoder().encodeToString(ciphertextCtr));
        byte[] decryptedCtr = doubleAesModes.ctrDecrypt(ciphertextCtr, iv);
        System.out.println("CTR decrypted: " + new String(decryptedCtr));
    }
}
