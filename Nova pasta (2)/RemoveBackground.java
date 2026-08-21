import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class RemoveBackground {
    public static void main(String[] args) {
        try {
            File inputFile = new File("monster.png");
            if (!inputFile.exists()) {
                System.out.println("Erro: monster.png nao encontrado.");
                return;
            }
            
            BufferedImage img = ImageIO.read(inputFile);
            int width = img.getWidth();
            int height = img.getHeight();
            
            BufferedImage newImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgba = img.getRGB(x, y);
                    Color col = new Color(rgba, true);
                    
                    // Se for branco ou muito claro (fundo branco)
                    if (col.getRed() > 240 && col.getGreen() > 240 && col.getBlue() > 240) {
                        // Transparente (Alpha = 0)
                        newImg.setRGB(x, y, 0x00000000);
                    } else {
                        newImg.setRGB(x, y, rgba);
                    }
                }
            }
            
            ImageIO.write(newImg, "png", new File("monster.png"));
            System.out.println("Fundo branco removido com sucesso de monster.png!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
