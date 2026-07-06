import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CutscenePanel — efeito de virar página de caderno (page-flip),
 * inspirado no hobby_quest_cutscene.html.
 */
public class CutscenePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {

    private JogoAudrey frame;
    private final int LARGURA = 1000;
    private final int ALTURA = 750;

    private int cenaAtual  = 0;
    private int cutsceneAtualId = 0;

    private Font fontCrayonHand;
    private Image[] imagens;
    private String[] nomesImagens = new String[0];

    // -------- Page-flip --------
    private enum FlipState { IDLE, FLIP_NEXT, FLIP_PREV }
    private FlipState flipState = FlipState.IDLE;
    private float flipT       = 0f;   // 0..1
    private int   flipFrom    = 0;
    private int   flipTo      = 0;
    private static final float FLIP_SPEED = 0.035f;  // incremento por frame (~60fps => ~28 frames)

    // -------- Sparkle / scratch particles --------
    private static final class Particle {
        float x, y, vx, vy, life, maxLife;
        Color color;
        Particle(float x, float y, Color c) {
            this.x = x; this.y = y;
            Random rnd = new Random();
            float ang = (float)(Math.random() * Math.PI * 2);
            float spd = 0.5f + (float)(Math.random() * 2f);
            vx = (float)(Math.cos(ang) * spd);
            vy = (float)(Math.sin(ang) * spd) - 1.5f;
            maxLife = 28 + (float)(Math.random() * 20);
            life = maxLife;
            color = c;
        }
    }
    private final List<Particle> particles = new ArrayList<>();
    private final Random rnd = new Random();

    // -------- Botão PULAR --------
    private int botaoPularX, botaoPularY;
    private final int botaoPularLargura = 130;
    private final int botaoPularAltura  = 40;
    private boolean botaoPularHover    = false;

    // -------- Dots indicator --------
    private final int DOT_SIZE = 10;
    private final int DOT_GAP  = 8;

    // -------- Botão "Começar aventura" (último slide) --------
    private boolean botaoIniciarHover  = false;
    private int pulseFrame = 0;

    // -------- Fade saída --------
    private boolean emFade = false;
    private int fadeProgresso = 0;
    private final int FADE_MAX = 40;
    private boolean fadeAcaoExecutada = false;

    // -------- Lápis animado --------
    private float pencilX = 0, pencilY = 0;
    private boolean pencilVisible = false;
    private int pencilTimer = 0;

    // -------- Cache dos slides renderizados (escala) --------
    // sem cache — desenhamos direto

    // -------------------------------------------------------
    public CutscenePanel(JogoAudrey frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        setDoubleBuffered(true);
        carregarFonts();
        new Timer(16, this).start();
    }

    // ======================= INIT ==========================

    public void iniciarCutscene(int id) {
        this.cutsceneAtualId = id;
        this.cenaAtual   = 0;
        this.flipState   = FlipState.IDLE;
        this.flipT       = 0f;
        this.particles.clear();
        this.emFade      = false;
        this.fadeProgresso = 0;
        this.fadeAcaoExecutada = false;
        this.pulseFrame  = 0;

        if (id == 0) {
            nomesImagens = new String[]{
                "slide1.png","slide2.png","slide3.png","slide4.png",
                "slide5.png","slide6.png","slide7.png","slide8.png",
                "slide9.png","slide10.png","slide11.png","slide12.png"
            };
        } else if (id == 1) {
            nomesImagens = new String[]{"cutscene_sala_1.png","cutscene_sala_2.png"};
        } else if (id == 2) {
            nomesImagens = new String[]{"cutscene_final_1.png","cutscene_final_2.png"};
        }

        imagens = new Image[nomesImagens.length];
        carregarImagens();
    }

    // ======================= FONTES / IMAGENS ==============

    private void carregarFonts() {
        try {
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new File("CrayonHandRegular2016Demo.ttf")).deriveFont(22f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontCrayonHand);
        } catch (Exception e) {
            fontCrayonHand = new Font("Arial", Font.BOLD, 22);
        }
    }

    private void carregarImagens() {
        for (int i = 0; i < nomesImagens.length; i++) {
            String nome = nomesImagens[i];
            File f = new File(nome);
            if (!f.exists()) f = new File("cutscene/" + nome);
            if (f.exists()) {
                imagens[i] = new ImageIcon(f.getAbsolutePath()).getImage();
            } else {
                imagens[i] = criarPlaceholder(i + 1);
            }
        }
    }

    private Image criarPlaceholder(int n) {
        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(238, 230, 212));
        g.fillRect(0, 0, 800, 600);
        g.setColor(new Color(120, 100, 80, 80));
        for (int y = 39; y < 600; y += 40) g.drawLine(0, y, 800, y);
        g.setColor(new Color(180, 150, 120));
        g.setFont(new Font("Arial", Font.BOLD, 48));
        String txt = "Slide " + n;
        FontMetrics fm = g.getFontMetrics();
        g.drawString(txt, (800 - fm.stringWidth(txt)) / 2, 300);
        g.dispose();
        return img;
    }

    // ======================= PAINT =========================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,  RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,      RenderingHints.VALUE_RENDER_QUALITY);

        // 1. Fundo papel bege
        desenharFundoPapel(g2d, w, h);

        // 2. Frame central (moldura do "livro")
        int frameW = Math.min(w - 20, (int)(h * 16.0 / 9.0));
        int frameH = (int)(frameW * 9.0 / 16.0);
        int frameX = (w - frameW) / 2;
        int frameY = (h - frameH) / 2;

        // Sombra da moldura
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRoundRect(frameX + 6, frameY + 10, frameW, frameH, 18, 18);

        // Clip da moldura
        Shape clip = new RoundRectangle2D.Float(frameX, frameY, frameW, frameH, 18, 18);
        g2d.setClip(clip);

        // 3. Desenhar slides com page-flip
        if (flipState == FlipState.IDLE) {
            desenharSlideEmRect(g2d, cenaAtual, frameX, frameY, frameW, frameH);
        } else {
            desenharPageFlip(g2d, frameX, frameY, frameW, frameH);
        }

        g2d.setClip(null);

        // 4. Borda da moldura
        g2d.setStroke(new BasicStroke(6f));
        g2d.setColor(new Color(58, 51, 42));
        g2d.drawRoundRect(frameX, frameY, frameW, frameH, 18, 18);
        g2d.setStroke(new BasicStroke(8f));
        g2d.setColor(new Color(23, 19, 15));
        g2d.drawRoundRect(frameX - 1, frameY - 1, frameW + 2, frameH + 2, 20, 20);
        g2d.setStroke(new BasicStroke(1f));

        // 5. Partículas sparkle
        desenharParticulas(g2d);

        // 6. Lápis animado
        if (pencilVisible) desenharLapis(g2d);

        // 7. Dots indicadores (dentro do frame, na parte de baixo)
        desenharDots(g2d, frameX, frameY, frameW, frameH);

        // 8. Botão PULAR
        atualizarPosicoesBotoes(w);
        desenharBotaoPular(g2d);

        // 9. Hint "clique para virar"
        if (imagens != null && cenaAtual < imagens.length - 1) {
            desenharHint(g2d, w, h);
        }

        // 10. Fade
        if (emFade) {
            int alpha;
            if (fadeProgresso < FADE_MAX) {
                alpha = fadeProgresso * 255 / FADE_MAX;
            } else {
                alpha = 255 - (fadeProgresso - FADE_MAX) * 255 / FADE_MAX;
            }
            g2d.setColor(new Color(0, 0, 0, Math.min(255, Math.max(0, alpha))));
            g2d.fillRect(0, 0, w, h);
        }
    }

    // ---- Fundo bege papel com linhas ----------------------
    private void desenharFundoPapel(Graphics2D g2d, int w, int h) {
        GradientPaint bg = new GradientPaint(0, 0, new Color(239, 230, 216), w, h, new Color(216, 205, 186));
        g2d.setPaint(bg);
        g2d.fillRect(0, 0, w, h);

        // Linhas de caderno
        g2d.setColor(new Color(216, 205, 186));
        g2d.setStroke(new BasicStroke(1f));
        for (int y = 40; y < h; y += 40) g2d.drawLine(0, y, w, y);

        // Decorações cantos
        g2d.setColor(new Color(210, 160, 180, 100));
        g2d.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        cutEstrela4(g2d, (int)(w * 0.03), (int)(h * 0.07), 7);
        cutEstrela4(g2d, (int)(w * 0.96), (int)(h * 0.88), 7);
        cutEstrela4(g2d, (int)(w * 0.97), (int)(h * 0.06), 6);
        cutEstrela4(g2d, (int)(w * 0.02), (int)(h * 0.90), 6);

        // Furos de espiral (lado esquerdo)
        g2d.setColor(new Color(23, 19, 15, 100));
        for (int iy = 8; iy < h; iy += 78) {
            g2d.fillOval(6, iy, 14, 14);
        }

        g2d.setStroke(new BasicStroke(1f));
    }

    // ---- Slide preenchendo o retângulo da moldura ---------
    private void desenharSlideEmRect(Graphics2D g2d, int idx, int rx, int ry, int rw, int rh) {
        if (imagens == null || idx < 0 || idx >= imagens.length || imagens[idx] == null) {
            g2d.setColor(new Color(238, 230, 212));
            g2d.fillRect(rx, ry, rw, rh);
            return;
        }
        Image img = imagens[idx];
        int iw = img.getWidth(this);
        int ih = img.getHeight(this);
        if (iw <= 0 || ih <= 0) {
            g2d.setColor(new Color(238, 230, 212));
            g2d.fillRect(rx, ry, rw, rh);
            return;
        }
        double scale = Math.max((double) rw / iw, (double) rh / ih);
        int fw = (int)(iw * scale);
        int fh = (int)(ih * scale);
        int ix = rx + (rw - fw) / 2;
        int iy = ry + (rh - fh) / 2;
        g2d.drawImage(img, ix, iy, fw, fh, this);

        // Animação do botão "Começar aventura" no último slide
        if (idx == imagens.length - 1) {
            desenharAnimacaoBotaoIniciar(g2d, ix, iy, fw, fh, scale);
        }
    }

    // ---- Page-flip 3D simulado com Java2D -----------------
    private void desenharPageFlip(Graphics2D g2d, int fx, int fy, int fw, int fh) {
        boolean goingNext = (flipState == FlipState.FLIP_NEXT);
        float t = easing(flipT);  // 0..1

        // slide "de baixo" (destino)
        desenharSlideEmRect(g2d, flipTo, fx, fy, fw, fh);

        // Sombra do understudyShade
        int shadowAlpha = (int)(t < 0.7f ? (0.7f - t) / 0.7f * 90 : 0);
        if (shadowAlpha > 0) {
            GradientPaint sh;
            if (goingNext) {
                sh = new GradientPaint(fx, fy, new Color(0,0,0,shadowAlpha), fx + fw*0.55f, fy, new Color(0,0,0,0));
            } else {
                sh = new GradientPaint(fx + fw, fy, new Color(0,0,0,shadowAlpha), fx + fw*0.45f, fy, new Color(0,0,0,0));
            }
            g2d.setPaint(sh);
            g2d.fillRect(fx, fy, fw, fh);
        }

        // ---- A página que está virando ----
        // Simulamos a virada como uma "dobra" vertical.
        // Para next: a página sai pela esquerda (dobra de 0→-180° em perspectiva)
        // Para prev: a página sai pela direita
        // Usamos shear + escala para simular perspectiva 2D.

        Graphics2D g3 = (Graphics2D) g2d.create();

        float halfAngle = t * 180f; // 0..180 graus
        float cosA      = (float) Math.cos(Math.toRadians(halfAngle));
        float absCos    = Math.abs(cosA);

        // Largura da página que aparece (projeta perspectiva)
        int pageW = (int)(fw * absCos);
        if (pageW < 2) pageW = 2;

        // Escolhe a imagem face: frente (from) até 90°, depois traseira (papel)
        boolean mostrandoFrente = (halfAngle <= 90f);

        if (goingNext) {
            // Pivot: borda direita do slide anterior = fx + fw
            int pivotX = fx + fw;
            // Página esquerda do pivot
            int pageX = pivotX - pageW;

            Shape oldClip = g3.getClip();
            g3.setClip(fx, fy, fw, fh);

            if (mostrandoFrente) {
                // Desenha slide "from" comprimido horizontalmente
                if (imagens[flipFrom] != null) {
                    int iw = imagens[flipFrom].getWidth(this);
                    int ih = imagens[flipFrom].getHeight(this);
                    double sc = Math.max((double) fw / iw, (double) fh / ih);
                    int srcW = (int)(iw * sc), srcH = (int)(ih * sc);
                    int srcX = fx + (fw - srcW) / 2, srcY = fy + (fh - srcH) / 2;

                    // Clip: apenas a parte direita que não virou
                    g3.setClip(pivotX - pageW, fy, fw - (fw - pageW), fh);

                    // Skew leve
                    AffineTransform at = new AffineTransform();
                    at.translate(pivotX, fy + fh / 2f);
                    at.scale(absCos, 1.0 - t * 0.04);
                    at.translate(-pivotX, -(fy + fh / 2f));
                    g3.setTransform(at);
                    g3.drawImage(imagens[flipFrom], srcX, srcY, srcW, srcH, this);
                    g3.setTransform(new AffineTransform());
                }
            } else {
                // Face traseira: papel de caderno
                desenharFaceTraseira(g3, pageX, fy, pageW, fh, absCos);
            }

            g3.setClip(oldClip);

            // Sombra de borda na dobra
            float edgeAlpha = (float)(Math.sin(Math.toRadians(halfAngle)) * 0.55);
            desenharSombraBorda(g3, pivotX - pageW, fy, pageW, fh, edgeAlpha, true);

        } else {
            // PREV: pivot esquerdo
            int pivotX = fx;
            int pageX  = pivotX;

            Shape oldClip = g3.getClip();
            g3.setClip(fx, fy, fw, fh);

            if (mostrandoFrente) {
                if (imagens[flipFrom] != null) {
                    int iw = imagens[flipFrom].getWidth(this);
                    int ih = imagens[flipFrom].getHeight(this);
                    double sc = Math.max((double) fw / iw, (double) fh / ih);
                    int srcW = (int)(iw * sc), srcH = (int)(ih * sc);
                    int srcX = fx + (fw - srcW) / 2, srcY = fy + (fh - srcH) / 2;

                    g3.setClip(fx, fy, pageW, fh);
                    AffineTransform at = new AffineTransform();
                    at.translate(pivotX, fy + fh / 2f);
                    at.scale(absCos, 1.0 - t * 0.04);
                    at.translate(-pivotX, -(fy + fh / 2f));
                    g3.setTransform(at);
                    g3.drawImage(imagens[flipFrom], srcX, srcY, srcW, srcH, this);
                    g3.setTransform(new AffineTransform());
                }
            } else {
                desenharFaceTraseira(g3, pageX, fy, pageW, fh, absCos);
            }

            g3.setClip(oldClip);
            desenharSombraBorda(g3, pageX + pageW - 15, fy, 15, fh, (float)(Math.sin(Math.toRadians(halfAngle)) * 0.55), false);
        }

        // Shadow "floor" (embaixo do frame) — faixa escura
        float shadowPeak = (float)(Math.sin(Math.toRadians(halfAngle)));
        int floorAlpha = (int)(shadowPeak * 60);
        if (floorAlpha > 0) {
            g3.setColor(new Color(0, 0, 0, floorAlpha));
            g3.fillRect(fx + fw / 4, fy + fh - 4, fw / 2, 6);
        }

        g3.dispose();
    }

    /** Face traseira da página (papel com linhas) */
    private void desenharFaceTraseira(Graphics2D g, int rx, int ry, int rw, int rh, float cosA) {
        // Fundo bege claro
        g.setColor(new Color(230, 221, 203));
        g.fillRect(rx, ry, rw, rh);
        // Linhas do caderno
        g.setColor(new Color(216, 205, 186));
        for (int y = ry + 39; y < ry + rh; y += 40) g.drawLine(rx, y, rx + rw, y);
        // Sombra radial no centro
        GradientPaint rg = new GradientPaint(rx + rw * 0.3f, ry + rh * 0.5f, new Color(0,0,0,(int)(15*cosA)),
                                              rx + rw, ry + rh * 0.5f, new Color(0,0,0,0));
        g.setPaint(rg);
        g.fillRect(rx, ry, rw, rh);
    }

    /** Sombra de borda (simula o vinco da página) */
    private void desenharSombraBorda(Graphics2D g, int x, int y, int w, int h, float alpha, boolean rightSide) {
        if (alpha <= 0 || w <= 0) return;
        int a = (int)(alpha * 200);
        GradientPaint sh;
        if (rightSide) {
            sh = new GradientPaint(x, y, new Color(0,0,0,Math.min(255,a)), x + Math.min(w, 40), y, new Color(0,0,0,0));
        } else {
            sh = new GradientPaint(x + w, y, new Color(0,0,0,Math.min(255,a)), x + Math.max(0, w - 40), y, new Color(0,0,0,0));
        }
        g.setPaint(sh);
        g.fillRect(x, y, w, h);
    }

    // ---- Animação do botão "Começar aventura" -------------
    private void desenharAnimacaoBotaoIniciar(Graphics2D g2d, int ix, int iy, int fw, int fh, double scale) {
        int btnIX = 541, btnIY = 909, btnIW = 786, btnIH = 78;
        int bx = ix + (int)(btnIX * scale);
        int by = iy + (int)(btnIY * scale);
        int bw = (int)(btnIW * scale);
        int bh = (int)(btnIH * scale);
        if (bw <= 0 || bh <= 0) return;
        int arc = (int)(bh * 0.7);

        float t = pulseFrame * 0.06f;
        float p1 = (float)(Math.sin(t) * 0.5 + 0.5);
        float p2 = (float)(Math.sin(t + 1.2) * 0.5 + 0.5);
        float p3 = (float)(Math.sin(t + 2.4) * 0.5 + 0.5);

        // Camada externa — brilho roxo escuro
        int a1 = (int)(40 + p1 * 80);
        g2d.setColor(new Color(75, 65, 135, a1));
        g2d.setStroke(new BasicStroke(4 + p2 * 6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawRoundRect(bx - 8, by - 8, bw + 16, bh + 16, arc + 4, arc + 4);

        // Camada média
        int a2 = (int)(60 + p2 * 100);
        g2d.setColor(new Color(120, 100, 200, a2));
        g2d.setStroke(new BasicStroke(2 + p3 * 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawRoundRect(bx - 4, by - 4, bw + 8, bh + 8, arc, arc);

        // Camada interna
        int a3 = (int)(80 + p1 * 120);
        g2d.setColor(new Color(180, 160, 230, a3));
        g2d.setStroke(new BasicStroke(1 + p2 * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawRoundRect(bx, by, bw, bh, arc, arc);

        // Partículas orbitais
        int cx = bx + bw / 2;
        int cy = by + bh / 2;
        int raioX = bw / 2 + 20;
        int raioY = bh / 2 + 12;
        g2d.setStroke(new BasicStroke(1f));
        for (int i = 0; i < 6; i++) {
            float ang = (float)(t * 0.8 + i * 1.047);
            float dist = 0.7f + (float)(Math.sin(t * 0.5 + i) * 0.3);
            int px = cx + (int)(Math.cos(ang) * raioX * dist);
            int py = cy + (int)(Math.sin(ang) * raioY * dist);
            int sz = (int)(2 + p3 * 3);
            g2d.setColor(new Color(140, 120, 200, (int)(100 + p1 * 120)));
            g2d.fillOval(px - sz / 2, py - sz / 2, sz, sz);
        }
        g2d.setStroke(new BasicStroke(2f));
    }

    // ---- Partículas ----------------------------------------
    private void desenharParticulas(Graphics2D g2d) {
        for (Particle p : particles) {
            float alpha = p.life / p.maxLife;
            int a = (int)(alpha * 200);
            int sz = (int)(3 + alpha * 5);
            g2d.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), Math.min(255, a)));
            g2d.fillOval((int)(p.x - sz / 2f), (int)(p.y - sz / 2f), sz, sz);
        }
    }

    // ---- Lápis animado (cursor) ----------------------------
    private void desenharLapis(Graphics2D g2d) {
        int px = (int) pencilX;
        int py = (int) pencilY;
        // corpo amarelo
        g2d.setColor(new Color(242, 201, 76));
        int[] xp = {px - 6, px + 6, px + 6, px - 6};
        int[] yp = {py - 20, py - 20, py + 12, py + 12};
        g2d.fillPolygon(xp, yp, 4);
        // ponta
        g2d.setColor(new Color(232, 185, 138));
        int[] xp2 = {px - 5, px + 5, px};
        int[] yp2 = {py + 12, py + 12, py + 22};
        g2d.fillPolygon(xp2, yp2, 3);
        // borracha (topo)
        g2d.setColor(new Color(242, 140, 160));
        g2d.fillRect(px - 6, py - 22, 12, 6);
        // borda
        g2d.setColor(new Color(138, 109, 30));
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawRect(px - 6, py - 20, 12, 32);
        g2d.setStroke(new BasicStroke(1f));
    }

    // ---- Dots indicadores ----------------------------------
    private void desenharDots(Graphics2D g2d, int fx, int fy, int fw, int fh) {
        if (imagens == null || imagens.length <= 1) return;
        int n = imagens.length;
        int totalW = n * DOT_SIZE + (n - 1) * DOT_GAP;
        int startX = fx + (fw - totalW) / 2;
        int dotY   = fy + fh - 22;
        for (int i = 0; i < n; i++) {
            int dx = startX + i * (DOT_SIZE + DOT_GAP);
            if (i == cenaAtual) {
                g2d.setColor(new Color(242, 166, 198)); // pink "on"
                g2d.fillOval(dx - 1, dotY - 1, DOT_SIZE + 2, DOT_SIZE + 2);
                // glow
                g2d.setColor(new Color(242, 166, 198, 60));
                g2d.fillOval(dx - 4, dotY - 4, DOT_SIZE + 8, DOT_SIZE + 8);
            } else {
                g2d.setColor(new Color(60, 50, 40, 65));
                g2d.fillOval(dx, dotY, DOT_SIZE, DOT_SIZE);
            }
        }
    }

    // ---- Botão PULAR ----------------------------------------
    private void atualizarPosicoesBotoes(int w) {
        botaoPularX = w - 158;
        botaoPularY = 20;
    }

    private void desenharBotaoPular(Graphics2D g2d) {
        Color corFundo     = new Color(45,  35,  95);
        Color corHover     = new Color(75,  65, 135);
        Color corTexto     = Color.WHITE;

        // fundo escuro atrás
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.fillRoundRect(botaoPularX - 10, botaoPularY - 6, botaoPularLargura + 20, botaoPularAltura + 12, 30, 30);

        g2d.setColor(botaoPularHover ? corHover : corFundo);
        g2d.fillRoundRect(botaoPularX, botaoPularY, botaoPularLargura, botaoPularAltura, botaoPularAltura, botaoPularAltura);

        g2d.setFont(fontCrayonHand.deriveFont(15f));
        g2d.setColor(corTexto);
        String txt = "PULAR \u23ED";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(txt,
            botaoPularX + (botaoPularLargura - fm.stringWidth(txt)) / 2,
            botaoPularY + ((botaoPularAltura - fm.getHeight()) / 2) + fm.getAscent());
    }

    // ---- Hint "clique para virar" --------------------------
    private void desenharHint(Graphics2D g2d, int w, int h) {
        float alpha = 0.55f + (float)(Math.sin(pulseFrame * 0.05) * 0.45);
        g2d.setFont(fontCrayonHand.deriveFont(12f));
        String txt = "clique / \u2192 para virar a p\u00e1gina";
        FontMetrics fm = g2d.getFontMetrics();
        int tw = fm.stringWidth(txt);
        int tx = w - tw - 24;
        int ty = h - 30;

        g2d.setColor(new Color(255, 255, 255, (int)(alpha * 160)));
        g2d.fillRoundRect(tx - 8, ty - fm.getAscent() - 2, tw + 16, fm.getHeight() + 4, 16, 16);
        g2d.setColor(new Color(60, 50, 40, (int)(alpha * 255)));
        g2d.drawString(txt, tx, ty);
    }

    // ---- Helper estrela 4 pontas ----------------------------
    private void cutEstrela4(Graphics2D g2d, int cx, int cy, int r) {
        int r2 = Math.max(2, r / 3);
        g2d.drawLine(cx - r, cy, cx + r, cy);
        g2d.drawLine(cx, cy - r, cx, cy + r);
        g2d.drawLine(cx - r2, cy - r2, cx + r2, cy + r2);
        g2d.drawLine(cx - r2, cy + r2, cx + r2, cy - r2);
    }

    // ---- Easing suave (cubic) --------------------------------
    private float easing(float t) {
        return t < 0.5f
            ? 4 * t * t * t
            : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
    }

    // ======================= ACTION (Timer) ==================

    @Override
    public void actionPerformed(ActionEvent e) {
        // Page-flip
        if (flipState != FlipState.IDLE) {
            flipT += FLIP_SPEED;
            if (flipT >= 1f) {
                flipT      = 0f;
                cenaAtual  = flipTo;
                flipState  = FlipState.IDLE;
                // Spawn sparkles no centro
                int w = getWidth(), h = getHeight();
                for (int i = 0; i < 12; i++) {
                    Color[] cols = {
                        new Color(242,166,198), new Color(143,191,92),
                        new Color(255,220,100), new Color(180,160,230)
                    };
                    particles.add(new Particle(w / 2f + (rnd.nextFloat() - 0.5f) * 200,
                                               h / 2f + (rnd.nextFloat() - 0.5f) * 100,
                                               cols[rnd.nextInt(cols.length)]));
                }
            }
        }

        // Partículas
        particles.removeIf(p -> {
            p.x += p.vx; p.y += p.vy; p.vy += 0.05f; p.life--;
            return p.life <= 0;
        });

        // Lápis
        if (pencilTimer > 0) {
            pencilTimer--;
            pencilVisible = pencilTimer > 0;
        }

        // Fade
        if (emFade) {
            fadeProgresso++;
            if (fadeProgresso >= FADE_MAX && !fadeAcaoExecutada) {
                fadeAcaoExecutada = true;
                iniciarJogo();
            }
            if (fadeProgresso >= FADE_MAX * 2) {
                emFade = false;
                fadeProgresso = 0;
                fadeAcaoExecutada = false;
            }
        }

        pulseFrame++;
        repaint();
    }

    // ======================= NAVEGAÇÃO =====================

    private void avancarSlide() {
        if (flipState != FlipState.IDLE || emFade) return;
        if (imagens != null && cenaAtual < imagens.length - 1) {
            GerenciadorAudio.tocarSomDialogo();
            flipFrom  = cenaAtual;
            flipTo    = cenaAtual + 1;
            flipT     = 0f;
            flipState = FlipState.FLIP_NEXT;
            pencilTimer = 20;
            pencilX = getWidth() * 0.5f;
            pencilY = getHeight() * 0.5f;
        } else {
            disparararFade();
        }
    }

    private void voltarSlide() {
        if (flipState != FlipState.IDLE || emFade) return;
        if (cenaAtual > 0) {
            GerenciadorAudio.tocarSomDialogo();
            flipFrom  = cenaAtual;
            flipTo    = cenaAtual - 1;
            flipT     = 0f;
            flipState = FlipState.FLIP_PREV;
        }
    }

    private void disparararFade() {
        if (!emFade) {
            emFade = true;
            fadeProgresso = 0;
            fadeAcaoExecutada = false;
        }
    }

    private void iniciarJogo() {
        GerenciadorAudio.tocarSomPlay();
        frame.voltarDeCutscene();
    }

    // ======================= HIT TEST (botão iniciar) =======

    private boolean isNoBotaoIniciar(int mx, int my) {
        if (imagens == null || cenaAtual != imagens.length - 1) return false;
        Image img = imagens[cenaAtual];
        int iw = img.getWidth(this), ih = img.getHeight(this);
        if (iw <= 0 || ih <= 0) return false;
        int w = getWidth(), h = getHeight();
        int frameW = Math.min(w - 20, (int)(h * 16.0 / 9.0));
        int frameH = (int)(frameW * 9.0 / 16.0);
        int frameX = (w - frameW) / 2, frameY = (h - frameH) / 2;
        double scale = Math.max((double) frameW / iw, (double) frameH / ih);
        int fw = (int)(iw * scale), fh = (int)(ih * scale);
        int ix = frameX + (frameW - fw) / 2, iy = frameY + (frameH - fh) / 2;
        int bx = ix + (int)(541 * scale), by = iy + (int)(909 * scale);
        int bw = (int)(786 * scale),      bh = (int)(78  * scale);
        return mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
    }

    // ======================= MOUSE =========================

    @Override
    public void mousePressed(MouseEvent e) {
        if (emFade || flipState != FlipState.IDLE) return;
        int mx = e.getX(), my = e.getY();

        // Botão PULAR
        if (mx >= botaoPularX && mx <= botaoPularX + botaoPularLargura &&
            my >= botaoPularY && my <= botaoPularY + botaoPularAltura) {
            disparararFade();
            return;
        }

        // Último slide: apenas botão "Começar"
        if (imagens != null && cenaAtual == imagens.length - 1) {
            if (isNoBotaoIniciar(mx, my)) disparararFade();
            return;
        }

        // Navegação por clique (metade direita / esquerda)
        if (mx > getWidth() / 2) {
            avancarSlide();
        } else if (cenaAtual > 0) {
            voltarSlide();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int mx = e.getX(), my = e.getY();

        boolean hp = mx >= botaoPularX && mx <= botaoPularX + botaoPularLargura &&
                     my >= botaoPularY && my <= botaoPularY + botaoPularAltura;
        boolean hi = isNoBotaoIniciar(mx, my);

        if (hp != botaoPularHover || hi != botaoIniciarHover) {
            botaoPularHover   = hp;
            botaoIniciarHover = hi;
            setCursor(new Cursor((hp || hi) ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            repaint();
        }

        // Move o lápis junto com o mouse (quando visível)
        if (pencilVisible) {
            pencilX = mx;
            pencilY = my;
        }
    }

    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {
        if (botaoPularHover || botaoIniciarHover) {
            botaoPularHover = false;
            botaoIniciarHover = false;
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            repaint();
        }
    }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}

    // ======================= TECLADO =======================

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_SPACE || k == KeyEvent.VK_ENTER) {
            avancarSlide();
        } else if (k == KeyEvent.VK_LEFT) {
            voltarSlide();
        } else if (k == KeyEvent.VK_ESCAPE) {
            int op = JOptionPane.showConfirmDialog(this,
                "Deseja voltar ao menu principal?", "Menu",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (op == JOptionPane.YES_OPTION) frame.voltarAoMenuPrincipal();
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
