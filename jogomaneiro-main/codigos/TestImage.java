import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class TestImage {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("Design sem nome.png"));
        System.out.println("Width: " + img.getWidth());
        System.out.println("Height: " + img.getHeight());
        System.out.println("Has Alpha: " + img.getColorModel().hasAlpha());
        
        int transparentCount = 0;
        int opaqueCount = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int p = img.getRGB(x, y);
                int alpha = (p >> 24) & 0xff;
                if (alpha < 128) transparentCount++;
                else opaqueCount++;
            }
        }
        System.out.println("Transparent pixels: " + transparentCount);
        System.out.println("Opaque pixels: " + opaqueCount);
    }
}
