import java.math.BigInteger;

public class BitCounter {
    public static void main(String[] args) {
        String md5Hash1 = args[0];
        String md5Hash2 = args[1];
        String sha256Hash1  = args[2];
        String sha256Hash2 = args[3];

        BigInteger md5Bits1 = new BigInteger(md5Hash1, 16);
        BigInteger sha256Bits1 = new BigInteger(sha256Hash1, 16);
        BigInteger md5Bits2 = new BigInteger(md5Hash2, 16);
        BigInteger sha256Bits2 = new BigInteger(sha256Hash2, 16);

        BigInteger xor1 = md5Bits1.xor(md5Bits2);
        BigInteger xor2 = sha256Bits1.xor(sha256Bits2);

        int sameBitsMD5 = 128 - xor1.bitCount();
        int sameBitsSHA256 = 256 - xor2.bitCount();

        System.out.println("Number of same bits for MD5: " + sameBitsMD5);
        System.out.println("Number of same bits for SHA256: " + sameBitsSHA256);
    }
}
