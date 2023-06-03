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

    public byte[] cfbEncrypt(byte[] plaintext, byte[] iv) throws Exception {
        // Apply PKCS#7 padding to the plaintext.
        byte[] paddedPlaintext = pad(plaintext);
        
        byte[] ciphertext = new byte[paddedPlaintext.length];
    
        // Initialize the shift register with the IV.
        byte[] shiftRegister = iv;
    
        // For each byte of plaintext,
        for (int i = 0; i < paddedPlaintext.length; i++) {
            // Encrypt the shift register using double AES.
            byte[] encryptedBlock = doubleAesEncrypt(shiftRegister);
    
            // XOR the first byte of the encrypted block with the current plaintext byte.
            byte ciphertextByte = (byte) (encryptedBlock[0] ^ paddedPlaintext[i]);
    
            // Add the ciphertext byte to the ciphertext array.
            ciphertext[i] = ciphertextByte;
    
            // Update the shift register: discard the first byte and append the ciphertext byte.
            shiftRegister = Arrays.copyOfRange(shiftRegister, 1, shiftRegister.length);
            shiftRegister = appendByte(shiftRegister, ciphertextByte);
        }
    
        // Return the final ciphertext.
        return ciphertext;
    }
    
    public byte[] cfbDecrypt(byte[] ciphertext, byte[] iv) throws Exception {
        byte[] plaintext = new byte[ciphertext.length];
    
        // Initialize the shift register with the IV.
        byte[] shiftRegister = iv;
    
        // For each byte of ciphertext,
        for (int i = 0; i < ciphertext.length; i++) {
            // Encrypt the shift register using double AES.
            byte[] encryptedBlock = doubleAesEncrypt(shiftRegister);
    
            // XOR the first byte of the encrypted block with the current ciphertext byte.
            byte plaintextByte = (byte) (encryptedBlock[0] ^ ciphertext[i]);
    
            // Add the plaintext byte to the plaintext array.
            plaintext[i] = plaintextByte;
    
            // Update the shift register: discard the first byte and append the current ciphertext byte.
            shiftRegister = Arrays.copyOfRange(shiftRegister, 1, shiftRegister.length);
            shiftRegister = appendByte(shiftRegister, ciphertext[i]);
        }
    
        // Unpad the plaintext before returning it.
        return unpad(plaintext);
    }

    // A helper function to append a byte to a byte array.
    public byte[] appendByte(byte[] array, byte b) {
        byte[] newArray = new byte[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[newArray.length - 1] = b;
        return newArray;
    }       
}
