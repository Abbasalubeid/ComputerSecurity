import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class StreamChipher {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Invalid number of arguments, args: <key> <infile> <outfile>");
            System.exit(1);
        }


    }

    private static byte[] getBytesFromFile(String file){
        Path inputFilePath = Path.of(file);
        if (!Files.exists(inputFilePath)) {
            System.out.println("Error: input file does not exist.");
            System.exit(1);
        }

        try {
            FileInputStream input = new FileInputStream(file);
            return input.readAllBytes();
        } catch (Exception e) {
            System.out.println("Could not read file");
            return null;
        }
    }
}
