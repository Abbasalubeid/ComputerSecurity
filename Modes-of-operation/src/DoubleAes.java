import java.nio.file.*;
import java.security.MessageDigest;

public class DoubleAes {
    
    public static void main(String[] args) {
        String key1 = null;
        String key2 = null;
        String method = null;
        String mode = null;
        Path input = null;
        Path output = null;

        for (String arg : args) {
            if (arg.startsWith("--key1=")) 
                key1 = arg.substring(7);
            else if (arg.startsWith("--key2=")) 
                key2 = arg.substring(7);
            else if (arg.startsWith("--input=")) 
                input = Paths.get(arg.substring(8));
            else if (arg.startsWith("--output=")) 
                output = Paths.get(arg.substring(9));
            else if (arg.startsWith("--mode=")) 
                mode = arg.substring(7);
            else if (arg.startsWith("--method=")) 
                method = arg.substring(9);
        }

        if (key1 == null || key2 == null || input == null || output == null || method == null || mode == null) {
            System.out.println("Missing required argument.");
            System.err.println("Usage: java DoubleAes --key1 <key1> --key2 <key2> --input <input file> --output <output file> --method <dec/enc> --mode <cbc/cfb/ctr>");
            System.exit(1);
        }
        DoubleAesModes modes = new DoubleAesModes(hashKey(key1), hashKey(key2));

        if (!Files.exists(input) || !Files.isRegularFile(input)) {
            System.out.println("Input file does not exist or is not a file.");
            System.exit(1);
        }
        byte[] inputData = null;
        try {
            inputData = Files.readAllBytes(input);
        } catch (Exception e) {
            System.err.println("Could not read file.");
            System.exit(1);
        }

        byte[] outputData = null;
        try {
            switch (method.toLowerCase()) {
                case "enc":
                    switch (mode.toLowerCase()) {
                        case "cbc":
                            outputData = modes.cbcEncrypt(inputData);
                            break;
                        case "cfb":
                            outputData = modes.cfbEncrypt(inputData);
                            break;
                        case "ctr":
                            outputData = modes.ctrEncrypt(inputData);
                            break;
                        default:
                            System.err.println("Unsupported encryption mode: " + method);
                            System.exit(1);
                    }
                    break;
                case "dec":
                    switch (mode.toLowerCase()) {
                        case "cbc":
                            outputData = modes.cbcDecrypt(inputData);
                            break;
                        case "cfb":
                            outputData = modes.cfbDecrypt(inputData);
                            break;
                        case "ctr":
                            outputData = modes.ctrDecrypt(inputData);
                            break;
                        default:
                            System.err.println("Unsupported decryption mode: " + method);
                            System.exit(1);
                    }
                    break;
                default:
                    System.err.println("Invalid method. Please use 'enc' for encryption or 'dec' for decryption.");
                    System.exit(1);
            }

            try {
                Files.write(output, outputData);
            } catch (Exception e) {
                System.err.println("Could not write to file.");
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("An unexpected error occurred, check that you are using the right files, methods, and modes");
            System.exit(1);
        }
    }

    // SHA-256 hash to the input key, transforming it to a fixed-length (32 bytes) representation to meet the length requirements of AES
    public static byte[] hashKey(String inputKey) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(inputKey.getBytes());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred, try another key");
            System.exit(1);
        }
        return null;
    }
}