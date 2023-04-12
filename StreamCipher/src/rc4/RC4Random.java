package rc4;
import java.util.Random;

public class RC4Random extends Random {
    private int[] s = new int[256];
    private int i = 0;
    private int j = 0;

    public RC4Random(byte[] key) {
        for (int k = 0; k < 256; k++) {
            s[k] = k;
        }
        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + s[i] + key[i % key.length]) % 256;
            int temp = s[i];
            s[i] = s[j];
            s[j] = temp;
        }
    }

    @Override
    public int next(int bits) {
        i = (i + 1) % 256;
        j = (j + s[i]) % 256;
        int temp = s[i];
        s[i] = s[j];
        s[j] = temp;
        int k = s[(s[i] + s[j]) % 256];
        return k & ((1 << bits) - 1);
    }


}
