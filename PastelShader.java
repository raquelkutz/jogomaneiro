import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class PastelShader {
    // Parâmetros do "shader" ajustáveis
    private static float EXPOSICAO = 1.15f;    // Aumenta brilho
    private static float SATURACAO = 0.65f;   // Reduz saturação (fosco)
    
    // Cor pêssego/cream normalizada: R=1.0, G=0.92, B=0.85
    private static float TINT_R = 1.0f;
    private static float TINT_G = 0.92f;
    private static float TINT_B = 0.85f;

    public static void aplicarFiltro(BufferedImage img) {
        // Obter os pixels de forma rápida
        int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        
        for (int i = 0; i < pixels.length; i++) {
            int color = pixels[i];
            
            int r = (color >> 16) & 0xff;
            int g = (color >> 8) & 0xff;
            int b = color & 0xff;
            
            // 1. Exposição (Brilho)
            r = (int)(r * EXPOSICAO);
            g = (int)(g * EXPOSICAO);
            b = (int)(b * EXPOSICAO);
            
            if (r > 255) r = 255;
            if (g > 255) g = 255;
            if (b > 255) b = 255;
            
            // 2. Saturação
            float luma = 0.299f * r + 0.587f * g + 0.114f * b;
            r = (int)(luma + SATURACAO * (r - luma));
            g = (int)(luma + SATURACAO * (g - luma));
            b = (int)(luma + SATURACAO * (b - luma));
            
            if (r < 0) r = 0; else if (r > 255) r = 255;
            if (g < 0) g = 0; else if (g > 255) g = 255;
            if (b < 0) b = 0; else if (b > 255) b = 255;
            
            // 3. Tint Pêssego/Cream
            r = (int)(r * TINT_R);
            g = (int)(g * TINT_G);
            b = (int)(b * TINT_B);
            
            if (r > 255) r = 255;
            if (g > 255) g = 255;
            if (b > 255) b = 255;
            
            // Recolocar no array (mantendo Alpha 255 para evitar transparência não intencional)
            pixels[i] = (0xFF000000) | (r << 16) | (g << 8) | b;
        }
    }
}
