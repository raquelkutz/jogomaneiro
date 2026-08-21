import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GenerateMonsterTexture {
    public static void main(String[] args) {
        try {
            int size = 128;
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fundo 100% transparente
            g.setColor(new Color(0, 0, 0, 0));
            g.fillRect(0, 0, size, size);

            // Capa vermelha (atrás do corpo)
            g.setColor(new Color(180, 20, 20));
            g.fillPolygon(new int[]{20, 50, 78, 108}, new int[]{38, 128, 128, 38}, 4);

            // Corpo - fantasia azul
            g.setColor(new Color(25, 60, 160));
            g.fillRoundRect(30, 40, 68, 70, 10, 10);

            // Faixa vermelha lateral
            g.setColor(new Color(180, 20, 20));
            g.fillRect(30, 40, 6, 70);
            g.fillRect(92, 40, 6, 70);

            // Barriga levemente saliente
            g.setColor(new Color(30, 70, 180));
            g.fillOval(34, 75, 60, 30);

            // Ombreiras douradas
            g.setColor(new Color(200, 160, 50));
            g.fillOval(22, 36, 22, 14);
            g.fillOval(94, 36, 22, 14);

            // Estrela branca no peito
            int cx = 64, cy = 65;
            int outerR = 16, innerR = 7;
            int[] starX = new int[10], starY = new int[10];
            for (int i = 0; i < 10; i++) {
                double angle = Math.PI / 2 + i * Math.PI / 5;
                int r = (i % 2 == 0) ? outerR : innerR;
                starX[i] = cx + (int)(r * Math.cos(angle));
                starY[i] = cy - (int)(r * Math.sin(angle));
            }
            g.setColor(Color.WHITE);
            g.fillPolygon(starX, starY, 10);

            // Calça preta
            g.setColor(new Color(15, 15, 20));
            g.fillRect(34, 108, 28, 20);
            g.fillRect(66, 108, 28, 20);

            // Pescoço
            g.setColor(new Color(220, 180, 140));
            g.fillRect(56, 28, 16, 14);

            // Cabeça
            g.setColor(new Color(220, 180, 140));
            g.fillOval(38, 4, 52, 52);

            // Cabelo loiro
            g.setColor(new Color(230, 200, 60));
            g.fillArc(38, 4, 52, 36, 0, 180);
            // Franja
            g.fillRect(40, 4, 48, 12);

            // Olhos
            g.setColor(new Color(30, 30, 80));
            g.fillOval(50, 22, 10, 8);
            g.fillOval(68, 22, 10, 8);
            g.setColor(Color.WHITE);
            g.fillOval(54, 23, 4, 3);
            g.fillOval(72, 23, 4, 3);

            // Sobrancelhas finas
            g.setColor(new Color(150, 110, 30));
            g.fillRect(49, 19, 12, 3);
            g.fillRect(68, 19, 12, 3);

            // Bigodinho fino
            g.setColor(new Color(100, 70, 20));
            g.fillRect(57, 37, 14, 3);

            // Boca fechada
            g.setColor(new Color(180, 120, 100));
            g.fillRect(58, 41, 12, 4);

            g.dispose();
            ImageIO.write(img, "png", new File("monster.png"));
            System.out.println("monster.png gerado com personagem!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
