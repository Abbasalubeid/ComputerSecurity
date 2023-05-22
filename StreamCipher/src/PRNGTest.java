import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class PRNGTest{
    public static void main(String[] args) throws Exception {
    
        final int SIZE = 512;
        BufferedImage myRandomImage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
        BufferedImage javaRandomImage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);

        MyRandom myRandom = new MyRandom(System.nanoTime());
        java.util.Random javaRandom = new java.util.Random();

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int myRandInt = myRandom.next(24);
                int javaRandInt = javaRandom.nextInt(1 << 24);
                myRandomImage.setRGB(x, y, myRandInt);
                javaRandomImage.setRGB(x, y, javaRandInt);
            }
        }

        ImageIO.write(myRandomImage, "PNG", new File("myRandom.png"));
        ImageIO.write(javaRandomImage, "PNG", new File("javaRandom.png"));
    }
}
