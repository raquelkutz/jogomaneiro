import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GenerateTexture {
    public static void main(String[] args) {
        try {
            int size = 128;
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                              java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            // Fundo amarelado
            g.setColor(new Color(200, 190, 80));
            g.fillRect(0, 0, size, size);
            
            // Padrão de setas e linhas
            for (int i = 0; i < size; i += 16) {
                g.setColor(new Color(180, 170, 60));
                g.fillRect(i, 0, 2, size);
                
                g.setColor(new Color(150, 140, 40));
                for (int j = 0; j < size; j += 16) {
                    g.fillPolygon(new int[]{i+4, i+8, i+12, i+8}, new int[]{j+8, j+2, j+8, j+5}, 4);
                    g.fillPolygon(new int[]{i+4, i+8, i+12, i+8}, new int[]{j+14, j+8, j+14, j+11}, 4);
                }
            }
            
            g.dispose();
            ImageIO.write(img, "png", new File("wall.png"));
            System.out.println("wall.png generated!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
