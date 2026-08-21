import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GenerateItems {
    public static void main(String[] args) {
        try {
            // Coin
            BufferedImage coin = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gc = coin.createGraphics();
            gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gc.setColor(new Color(0, 0, 0, 0));
            gc.fillRect(0, 0, 128, 128);
            
            // Outer gold ring
            gc.setColor(new Color(210, 180, 20));
            gc.fillOval(16, 16, 96, 96);
            // Inner green (robux style)
            gc.setColor(new Color(20, 160, 50));
            gc.fillOval(24, 24, 80, 80);
            // Dollar/R symbol
            gc.setColor(Color.WHITE);
            gc.setFont(gc.getFont().deriveFont(48f));
            gc.drawString("R$", 38, 80);
            gc.dispose();
            ImageIO.write(coin, "png", new File("coin.png"));
            
            // Portal
            BufferedImage portal = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gp = portal.createGraphics();
            gp.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gp.setColor(new Color(0, 0, 0, 0));
            gp.fillRect(0, 0, 128, 128);
            
            gp.setColor(new Color(150, 0, 255));
            gp.fillOval(10, 10, 108, 108);
            gp.setColor(new Color(50, 0, 150));
            gp.fillOval(24, 24, 80, 80);
            gp.setColor(new Color(200, 150, 255));
            for(int i=0; i<8; i++) {
                gp.drawOval(10 + i*6, 10 + i*6, 108 - i*12, 108 - i*12);
            }
            gp.dispose();
            ImageIO.write(portal, "png", new File("portal.png"));
            
            System.out.println("Texturas geradas com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
