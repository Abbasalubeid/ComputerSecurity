import java.util.Random;

public class MyRandom extends Random {
    private int[] s = new int[256];
    private int i = 0;
    private int j = 0;

    public MyRandom(byte[] key) {
        for (int k = 0; k < 256; k++) {
            s[k] = k;
        }
        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + s[i] + (key[i % key.length] & 0xFF)) & 0xFF;
            int temp = s[i];
            s[i] = s[j];
            s[j] = temp;
        }
    }

    public int next(int bits) {
        i = (i + 1) & 0xFF;
        j = (j + s[i]) & 0xFF;
        int temp = s[i];
        s[i] = s[j];
        s[j] = temp;
        int k = s[(s[i] + s[j]) & 0xFF];
        return k & ((1 << bits) - 1);
    }
}
