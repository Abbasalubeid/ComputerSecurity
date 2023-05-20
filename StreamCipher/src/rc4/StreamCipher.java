import java.io.*;
import java.math.BigInteger;

public class StreamCipher {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Invalid number of arguments, args: <key> <infile> <outfile>");
            System.exit(1);
        }
        BigInteger key;
        try {
            key = new BigInteger(args[0]);
            cipher(key.toByteArray(), args[1], args[2]);
        } catch (Exception e) {
            System.err.println("Invalid key");
            System.exit(1);
        }
    }

    private static void cipher(byte[] key, String inputFile, String outputFile) {
        try {
            File file = new File(inputFile);
            if (!file.exists()) {
                System.err.println("Input file does not exist");
                System.exit(1);
            }

            MyRandom rnd = new MyRandom(key);
            try (FileInputStream in = new FileInputStream(file);
                 FileOutputStream out = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) != -1) {
                    for (int i = 0; i < length; i++) {
                        buffer[i] = (byte) (buffer[i] ^ rnd.next(8));
                    }
                    out.write(buffer, 0, length);
                }
            }

        } catch (IOException e) {
            System.err.println("Encryption fail " + e);
            System.exit(1);
        }
    }
}
