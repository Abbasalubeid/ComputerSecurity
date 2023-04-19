import java.math.BigInteger;

public class BitCounter {
    public static void main(String[] args) {
        String md5Hash1 = "440658b6d478ec92f7724c6287e53224";
        String sha256Hash1 = "e577b1a4bd1f8e662e7d20f604e9e9f6f6f68acb8473f3537b900a60699c729b";
        String md5Hash2 = "c53ebeec5d02f1f5a63314c069e2f970";
        String sha256Hash2 = "07528c1d3139dae813186b203bf89ca7e3ec3a199f2ae0e8cf24c73ca021680e";

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
