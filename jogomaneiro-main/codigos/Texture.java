import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Texture {
    public int[] pixels;
    public int SIZE;
    public boolean hasAlpha;

    public Texture(String filepath, int size) {
        this.SIZE = size;
        pixels = new int[SIZE * SIZE];
        load(filepath);
    }

    private void load(String filepath) {
        try {
            BufferedImage image = ImageIO.read(new File(filepath));
            hasAlpha = image.getColorModel().hasAlpha();

            int type = hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            BufferedImage scaled = new BufferedImage(SIZE, SIZE, type);
            java.awt.Graphics2D g2d = scaled.createGraphics();
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                               java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                               java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            int imgW = image.getWidth();
            int imgH = image.getHeight();
            int targetW = SIZE;
            int targetH = SIZE;
            if (imgW > imgH) {
                targetH = (int)(SIZE * ((double)imgH / imgW));
            } else {
                targetW = (int)(SIZE * ((double)imgW / imgH));
            }
            int x = (SIZE - targetW) / 2;
            int y = (SIZE - targetH) / 2;
            g2d.drawImage(image, x, y, targetW, targetH, null);
            g2d.dispose();
            scaled.getRGB(0, 0, SIZE, SIZE, pixels, 0, SIZE);
        } catch (Exception e) {
            System.out.println("Aviso: Textura '" + filepath + "' não encontrada. Usando textura gerada.");
            for (int i = 0; i < SIZE * SIZE; i++) {
                int x = i % SIZE;
                int y = i / SIZE;
                if (x == 0 || y == 0) pixels[i] = new Color(120, 100, 30).getRGB();
                else pixels[i] = new Color(180, 160, 60).getRGB();
            }
        }
    }
}
