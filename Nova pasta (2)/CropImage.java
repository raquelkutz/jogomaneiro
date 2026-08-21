import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class CropImage {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("Design sem nome.png"));
        int minX = img.getWidth(), minY = img.getHeight(), maxX = 0, maxY = 0;
        
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int alpha = (img.getRGB(x, y) >> 24) & 0xff;
                if (alpha > 128) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        
        if (minX <= maxX && minY <= maxY) {
            BufferedImage cropped = img.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
            ImageIO.write(cropped, "png", new File("moeda_cropped.png"));
            System.out.println("Cropped successfully to " + cropped.getWidth() + "x" + cropped.getHeight());
        } else {
            System.out.println("Image is fully transparent.");
        }
    }
}
