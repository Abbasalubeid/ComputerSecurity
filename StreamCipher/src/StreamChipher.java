import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class StreamChipher {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Invalid number of arguments, args: <key> <infile> <outfile>");
            System.exit(1);
        }
        int key = 0;
        try {
        key = Integer.parseInt(args[0]);

        } catch (Exception e) {
            System.err.println("Invalid key");
            System.exit(1);
        }
        byte[] m = getBytesFromFile(args[1]);
        byte [] c = cipher(key, m);
        writeBytesToFile(args[2], c);

    }

    private static byte[] getBytesFromFile(String file){
        Path inputFilePath = Path.of(file);
        if (!Files.exists(inputFilePath)) {
            System.err.println("Input file does not exist");
            System.exit(1);
        }

        try {
            FileInputStream input = new FileInputStream(file);
            byte[] data = input.readAllBytes();
            input.close();
            return data;
        } catch (Exception e) {
            System.err.println("Could not read file");
            return null;
        }
    }

    private static void writeBytesToFile(String file, byte[] bytesToWrite){
        Path outputFilePath = Path.of(file);
        if (Files.exists(outputFilePath)) {
            System.err.println("Warning: output file already exists and can be overwritten");
        }
        try {
            FileOutputStream output = new FileOutputStream(file);
            output.write(bytesToWrite);
            output.close();
        } catch (Exception e) {
            System.err.println("Could not write to file");
            System.exit(1);
        }
    }

    private static byte[] cipher(int seed, byte[] plaintext){
        try {
            MyRandom rnd = new MyRandom(seed);
            byte[] cipherBytes = new byte[plaintext.length];
            for (int i = 0; i < plaintext.length; i++) {
                cipherBytes[i] = (byte) (plaintext[i] ^ rnd.next(8));
            }
            return cipherBytes;
        } catch (Exception e) {
            System.err.println("Encryption fail: " + e);
            return null;
        }


        
    }
}
