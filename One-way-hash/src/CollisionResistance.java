import java.security.*;

public class CollisionResistance {
    public static void main(String[] args) {

        String[] messages = {"IV1013 security", "Security is fun", "Yes, indeed", "Secure IV1013", "No way"};
        int[] trials = new int[messages.length];

        for (int i = 0; i < messages.length; i++) {
            String message = messages[i];
            byte[] hash = getHash(message);

            int count = 0;
            while (true) {
                String randomMessage = getRandomString();
                byte[] randomHash = getHash(randomMessage);
                count++;

                if (checkCollision(hash, randomHash)) {
                    trials[i] = count;
                    break;
                }
            }
            System.out.println("Message: " + message + ", Trials: " + trials[i]);
        }
    }

    public static byte[] getHash(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(message.getBytes());
            return hash;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean checkCollision(byte[] hash1, byte[] hash2) {
        int len = 3; // use first 24 bits
        for (int i = 0; i < len; i++) {
            if (hash1[i] != hash2[i]) {
                return false;
            }
        }
        return true;
    }

    public static String getRandomString() {
        int length = 10;
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }
}
