public class StreamChipher {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Error: invalid number of arguments.");
            System.err.println("args: <key> <infile> <outfile>");
            System.exit(1);
        }
    }
}
