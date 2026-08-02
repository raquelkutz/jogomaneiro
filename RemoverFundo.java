import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class RemoverFundo {
    public static void main(String[] args) throws Exception {
        String[] arquivos = { "andar 1.png", "andar 2.png" };

        for (String arquivo : arquivos) {
            BufferedImage img = ImageIO.read(new File(arquivo));
            int w = img.getWidth();
            int h = img.getHeight();

            BufferedImage resultado = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int rgb = img.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;

                    // Remove pixels pretos ou quase pretos (threshold ajustavel)
                    if (r < 30 && g < 30 && b < 30) {
                        resultado.setRGB(x, y, 0x00000000); // transparente
                    } else {
                        resultado.setRGB(x, y, rgb);
                    }
                }
            }

            ImageIO.write(resultado, "PNG", new File(arquivo));
            System.out.println("Fundo removido: " + arquivo);
        }
        System.out.println("Concluido!");
    }
}
