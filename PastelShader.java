import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * PastelShader — tradução otimizada do shader GLSL (god rays, bloom, fog,
 * warmth, vignette, grain) para manipulação de pixels em Java 2D.
 * 
 * PERFORMANCE: Processa em blocos 2x2, god rays com 6 amostras,
 * bloom pré-calculado em blocos 8x8, frame-skipping a cada 2 frames.
 */
public class PastelShader {

    // Parâmetros ajustáveis (0..100 mapeados para 0..1 internamente)
    public static int rayStrength = 45;   // 0-100
    public static int bloom        = 60;  // 0-100
    public static int fog          = 25;  // 0-100
    public static int warmth       = 80;  // 0-100
    public static int vignette     = 65;  // 0-100

    // Posição do sol em UV (0..1)
    private static float sunU = 0.72f;
    private static float sunV = 0.78f;

    private static long frameCount = 0;
    
    // Frame-skipping: removido para evitar piscar na tela
    private static int frameSkip = 0; 

    public static void aplicarFiltro(BufferedImage img) {
        frameCount++;

        int width  = img.getWidth();
        int height = img.getHeight();
        int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();

        float time = frameCount * 0.016f;

        // Mapear 0-100 para floats internos
        float uRay  = rayStrength / 100f;
        float uBlm  = bloom / 100f;
        float uFog  = fog / 100f;
        float uWrm  = warmth / 100f;
        float uVig  = vignette / 100f;

        // Se tudo está zerado, não processar
        if (uRay == 0 && uBlm == 0 && uFog == 0 && uWrm == 0 && uVig == 0) return;

        // ---- Bloom pré-calculado em blocos 8x8 (rápido) ----
        int bsz = 8;
        int bw = (width + bsz - 1) / bsz;
        int bh = (height + bsz - 1) / bsz;
        float[] blR = new float[bw * bh];
        float[] blG = new float[bw * bh];
        float[] blB = new float[bw * bh];

        if (uBlm > 0.01f) {
            for (int by = 0; by < bh; by++) {
                for (int bx = 0; bx < bw; bx++) {
                    float sr = 0, sg = 0, sb = 0, tw = 0;
                    int cx = bx * bsz + bsz / 2;
                    int cy = by * bsz + bsz / 2;
                    // Amostra 3x3 esparsada
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            int sx = clampI(cx + dx * bsz, 0, width - 1);
                            int sy = clampI(cy + dy * bsz, 0, height - 1);
                            int c = pixels[sy * width + sx];
                            float pr = ((c >> 16) & 0xff) * 0.00392157f;
                            float pg = ((c >> 8) & 0xff) * 0.00392157f;
                            float pb = (c & 0xff) * 0.00392157f;
                            float brt = 0.299f * pr + 0.587f * pg + 0.114f * pb;
                            float w = brt > 0.55f ? (brt - 0.55f) / 0.45f : 0f; // smoothstep simplificado
                            w = w * w;
                            sr += pr * w; sg += pg * w; sb += pb * w;
                            tw += w + 0.0001f;
                        }
                    }
                    int idx = by * bw + bx;
                    blR[idx] = sr / tw;
                    blG[idx] = sg / tw;
                    blB[idx] = sb / tw;
                }
            }
        }

        // ---- Pre-computar vinheta por linha (evita sqrt por pixel) ----
        float[] vigRow = null;
        if (uVig > 0.01f) {
            vigRow = new float[height];
            for (int y = 0; y < height; y++) {
                float dy = (float)y / height - 0.5f;
                vigRow[y] = dy * dy;
            }
        }

        // ---- Aplicar efeitos em blocos 2x2 para performance ----
        int step = 2; // Processar em blocos 2x2
        for (int y = 0; y < height; y += step) {
            float v = (float) y / height;
            float fogBase = uFog > 0.01f ? uFog * smoothstepFast(1f - v) * 0.5f : 0f;
            float vigDy2 = vigRow != null ? vigRow[y] : 0;

            for (int x = 0; x < width; x += step) {
                int i = y * width + x;
                int color = pixels[i];

                float r = ((color >> 16) & 0xff) * 0.00392157f;
                float g = ((color >> 8) & 0xff) * 0.00392157f;
                float b = (color & 0xff) * 0.00392157f;

                float u = (float) x / width;

                // 1. God Rays (Light Shafts)
                if (uRay > 0.01f) {
                    float toSunU = u - sunU;
                    float toSunV = v - sunV;
                    int SAMPLES = 12; // Aumentado para melhor qualidade
                    float decay = 0.96f;
                    float stepU = (toSunU / SAMPLES) * -1.0f;
                    float stepV = (toSunV / SAMPLES) * -1.0f;
                    float pu = u, pv = v;
                    float weight = 1.0f;
                    float rayR = 0, rayG = 0, rayB = 0;
                    
                    for (int s = 0; s < SAMPLES; s++) {
                        pu += stepU * 0.5f; 
                        pv += stepV * 0.5f;
                        int sx = (int)(clampF(pu, 0f, 1f) * (width - 1));
                        int sy = (int)(clampF(pv, 0f, 1f) * (height - 1));
                        int sc = pixels[sy * width + sx];
                        float sr = ((sc >> 16) & 0xff) * 0.00392157f;
                        float sg = ((sc >> 8) & 0xff) * 0.00392157f;
                        float sb = (sc & 0xff) * 0.00392157f;
                        float brightness = 0.3f * sr + 0.3f * sg + 0.4f * sb;
                        
                        rayR += sr * brightness * weight;
                        rayG += sg * brightness * weight;
                        rayB += sb * brightness * weight;
                        weight *= decay;
                    }
                    float scaleRay = (2.0f / SAMPLES) * (uRay * 2.0f); // uRay ajustado para ser mais visível no Java
                    r += rayR * scaleRay;
                    g += rayG * scaleRay;
                    b += rayB * scaleRay;
                }

                // 2. Bloom
                if (uBlm > 0.01f) {
                    int bi = (y / bsz) * bw + (x / bsz);
                    float bm = uBlm * 0.6f;
                    r += blR[bi] * bm;
                    g += blG[bi] * bm;
                    b += blB[bi] * bm;
                }

                // 3. Neblina
                if (fogBase > 0.001f) {
                    r += (0.95f - r) * fogBase;
                    g += (0.75f - g) * fogBase;
                    b += (0.70f - b) * fogBase;
                }

                // 4. Warmth
                if (uWrm > 0.01f) {
                    float wR = r * (1.0f + 0.15f * uWrm);
                    float wG = g * (1.0f + 0.02f * uWrm);
                    float wB = b * (1.0f - 0.05f * uWrm);
                    float luma = 0.299f * wR + 0.587f * wG + 0.114f * wB;
                    // color = mix(vec3(luma), color, 1.15);
                    r = luma + (wR - luma) * 1.15f;
                    g = luma + (wG - luma) * 1.15f;
                    b = luma + (wB - luma) * 1.15f;
                }

                // 5. Tonemap
                r = r / (1f + r * 0.8f);
                g = g / (1f + g * 0.8f);
                b = b / (1f + b * 0.8f);
                // Math.pow para mais precisão
                r = (float) Math.pow(r, 0.9);
                g = (float) Math.pow(g, 0.9);
                b = (float) Math.pow(b, 0.9);

                // 6. Vinheta
                if (uVig > 0.01f) {
                    float dx = u - 0.5f;
                    float dy = v - 0.5f;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    float vig = smoothstepReverse(0.95f, 0.2f, dist * uVig * 1.5f); // 1.5f factor to match GLSL strength
                    r *= vig; g *= vig; b *= vig;
                }

                // 7. Grão
                float grain = (fastHash(x + frameCount * 60, y + frameCount * 60) - 0.5f) * 0.025f;
                r += grain; g += grain; b += grain;

                int ri = r <= 0 ? 0 : r >= 1 ? 255 : (int)(r * 255f);
                int gi = g <= 0 ? 0 : g >= 1 ? 255 : (int)(g * 255f);
                int bi2 = b <= 0 ? 0 : b >= 1 ? 255 : (int)(b * 255f);
                int finalColor = 0xFF000000 | (ri << 16) | (gi << 8) | bi2;

                // Preencher bloco 2x2 com a mesma cor
                pixels[i] = finalColor;
                if (x + 1 < width) {
                    pixels[i + 1] = finalColor;
                }
                if (y + 1 < height) {
                    pixels[i + width] = finalColor;
                    if (x + 1 < width) {
                        pixels[i + width + 1] = finalColor;
                    }
                }
            }
        }
    }

    // ============ Utilitários otimizados ============
    private static float clampF(float v, float min, float max) {
        return v < min ? min : (v > max ? max : v);
    }

    private static int clampI(int v, int min, int max) {
        return v < min ? min : (v > max ? max : v);
    }

    // smoothstep(0, 1, x) simplificado
    private static float smoothstepFast(float x) {
        if (x <= 0) return 0;
        if (x >= 1) return 1;
        return x * x * (3f - 2f * x);
    }

    // smoothstep(edge0, edge1, x) invertido
    private static float smoothstepReverse(float edge0, float edge1, float x) {
        float t = (x - edge0) / (edge1 - edge0);
        if (t < 0) t = 0; else if (t > 1) t = 1;
        return t * t * (3f - 2f * t);
    }

    // pow(x, 0.9) aproximado via sqrt blend
    private static float fastPow09(float x) {
        if (x <= 0) return 0;
        // Aproximação: x^0.9 ≈ x * (1 + 0.1 * (1/x^0.1 - 1)) -> simplificado
        return x * (1.0f + 0.05f * (1.0f - x));
    }

    // Hash rápido baseado em operações inteiras
    private static float fastHash(long x, long y) {
        long h = x * 374761393L + y * 668265263L;
        h = (h ^ (h >> 13)) * 1274126177L;
        return (float)((h & 0x7FFFFFFFL) * 4.6566128752e-10);
    }
}
