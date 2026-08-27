import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Texture {
    public int[] pixels;
    public int SIZE;
    public boolean hasAlpha;

    public Texture() {}

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
            Graphics2D g2d = scaled.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                               RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                               RenderingHints.VALUE_RENDER_QUALITY);
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

    public static Texture createGraffitiTexture(Texture base, String line1, String line2, Color color) {
        Texture tex = new Texture();
        tex.SIZE = base.SIZE;
        tex.pixels = new int[base.SIZE * base.SIZE];
        System.arraycopy(base.pixels, 0, tex.pixels, 0, base.pixels.length);

        BufferedImage img = new BufferedImage(base.SIZE, base.SIZE, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, base.SIZE, base.SIZE, base.pixels, 0, base.SIZE);

        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int cx = base.SIZE / 2;
        int cy = base.SIZE / 2;

        if (line1 != null && !line1.isEmpty()) {
            g2.setFont(new Font("Impact", Font.BOLD, Math.max(14, (int)(base.SIZE * 0.08))));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(line1);
            int tx = cx - tw / 2;
            int ty = cy - (line2 != null ? 8 : 0);

            // Sombra do spray
            g2.setColor(new Color(0, 0, 0, 200));
            g2.drawString(line1, tx + 3, ty + 3);

            // Tinta spray vermelha
            g2.setColor(color != null ? color : new Color(210, 25, 25));
            g2.drawString(line1, tx, ty);

            // Gotas de tinta escorrendo da pixação
            g2.fillRect(tx + (int)(tw * 0.2), ty + 3, 2, 22);
            g2.fillRect(tx + (int)(tw * 0.6), ty + 3, 3, 16);
            g2.fillRect(tx + (int)(tw * 0.85), ty + 3, 2, 28);
        }

        if (line2 != null && !line2.isEmpty()) {
            g2.setFont(new Font("Impact", Font.BOLD, Math.max(12, (int)(base.SIZE * 0.065))));
            FontMetrics fm2 = g2.getFontMetrics();
            int tw2 = fm2.stringWidth(line2);
            int tx2 = cx - tw2 / 2;
            int ty2 = cy + 24;

            g2.setColor(new Color(0, 0, 0, 200));
            g2.drawString(line2, tx2 + 2, ty2 + 2);

            g2.setColor(color != null ? color : new Color(180, 20, 20));
            g2.drawString(line2, tx2, ty2);

            g2.fillRect(tx2 + (int)(tw2 * 0.4), ty2 + 2, 2, 18);
        }

        // Respingos de sangue e marcas assustadoras
        g2.setColor(new Color(150, 10, 10, 160));
        g2.fillOval((int)(base.SIZE * 0.12), (int)(base.SIZE * 0.15), 18, 24);
        g2.fillOval((int)(base.SIZE * 0.82), (int)(base.SIZE * 0.68), 14, 16);

        g2.dispose();
        img.getRGB(0, 0, base.SIZE, base.SIZE, tex.pixels, 0, base.SIZE);
        return tex;
    }

    public static Texture createDoorTexture(Texture base, boolean locked) {
        Texture tex = new Texture();
        tex.SIZE = base.SIZE;
        tex.pixels = new int[base.SIZE * base.SIZE];

        BufferedImage img = new BufferedImage(base.SIZE, base.SIZE, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, base.SIZE, base.SIZE, base.pixels, 0, base.SIZE);

        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int s = base.SIZE;
        int framePadX = (int)(s * 0.16);
        int framePadY = (int)(s * 0.08);
        int doorW = s - framePadX * 2;
        int doorH = s - framePadY;

        // Moldura da porta (madeira escura)
        g2.setColor(new Color(35, 22, 12));
        g2.fillRect(framePadX - 8, framePadY - 8, doorW + 16, doorH + 8);
        g2.setColor(new Color(15, 8, 4));
        g2.drawRect(framePadX - 8, framePadY - 8, doorW + 16, doorH + 8);

        if (!locked) {
            // Vão da porta aberta (passagem escura)
            g2.setColor(new Color(12, 10, 15));
            g2.fillRect(framePadX, framePadY, doorW, doorH);

            // Folha da porta entreaberta no canto esquerdo
            g2.setColor(new Color(70, 44, 25));
            g2.fillRect(framePadX, framePadY, (int)(doorW * 0.28), doorH);
            g2.setColor(new Color(30, 18, 10));
            g2.drawRect(framePadX, framePadY, (int)(doorW * 0.28), doorH);
        } else {
            // Porta trancada: painel de madeira completo
            g2.setColor(new Color(80, 50, 26));
            g2.fillRect(framePadX, framePadY, doorW, doorH);

            // Painéis entalhados da porta
            g2.setColor(new Color(50, 30, 15));
            g2.drawRect(framePadX + 6, framePadY + 8, doorW - 12, (int)(doorH * 0.4));
            g2.drawRect(framePadX + 6, framePadY + (int)(doorH * 0.46), doorW - 12, (int)(doorH * 0.48));

            // Maçaneta e Fechadura
            int handleY = framePadY + (int)(doorH * 0.52);
            int handleX = framePadX + (int)(doorW * 0.82);
            g2.setColor(new Color(210, 175, 75));
            g2.fillOval(handleX - 4, handleY - 4, 10, 10);
            g2.setColor(new Color(40, 25, 10));
            g2.fillRect(handleX - 1, handleY + 6, 3, 7);

            // Placa TRANCADA em vermelho
            g2.setColor(new Color(150, 20, 20));
            int signW = (int)(doorW * 0.72);
            int signH = 22;
            int signX = framePadX + (doorW - signW) / 2;
            int signY = framePadY + (int)(doorH * 0.25);
            g2.fillRect(signX, signY, signW, signH);
            g2.setColor(Color.WHITE);
            g2.drawRect(signX, signY, signW, signH);

            g2.setFont(new Font("Arial", Font.BOLD, Math.max(10, (int)(s * 0.05))));
            FontMetrics fm = g2.getFontMetrics();
            String txt = "TRANCADA";
            g2.drawString(txt, signX + (signW - fm.stringWidth(txt)) / 2, signY + 15);
        }

        g2.dispose();
        img.getRGB(0, 0, base.SIZE, base.SIZE, tex.pixels, 0, base.SIZE);
        return tex;
    }
}
