import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

/**
 * CutscenePanel — transição fluida entre cenas com efeito suave e partículas.
 */
public class CutscenePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {

    private JogoAudrey frame;
    private final int LARGURA = 1000;
    private final int ALTURA = 750;

    private int cenaAtual = 0;
    private int cutsceneAtualId = 0;

    private Font fontCrayonHand;
    private Image[] imagens;
    private String[] nomesImagens = new String[0];

    // -------- Transição de Slide --------
    private enum FlipState {
        IDLE, FLIP_NEXT, FLIP_PREV
    }
    private FlipState flipState = FlipState.IDLE;
    private float flipT = 0f;   // 0..1
    private int flipFrom = 0;
    private int flipTo = 0;
    private static final float FLIP_SPEED = 0.055f;  // ~18 frames (~0.3s) - transição rápida e fluida

    private static final Random SHARED_RND = new Random();
    private static final BasicStroke STROKE_6 = new BasicStroke(6f);
    private static final BasicStroke STROKE_8 = new BasicStroke(8f);
    private static final BasicStroke STROKE_1 = new BasicStroke(1f);
    private static final Color COLOR_MOLDURA_INNER = new Color(75, 65, 135);
    private static final Color COLOR_MOLDURA_OUTER = new Color(45, 35, 95);
    private static final Color[] SPARK_COLORS = {
        new Color(150, 130, 230), new Color(200, 190, 255),
        new Color(120, 80, 220), new Color(180, 160, 230)
    };

    // -------- Partículas --------
    private static final class Particle {
        float x, y, vx, vy, life, maxLife;
        Color color;

        Particle(float x, float y, Color c) {
            this.x = x;
            this.y = y;
            float ang = (float) (SHARED_RND.nextFloat() * Math.PI * 2);
            float spd = 0.8f + (float) (SHARED_RND.nextFloat() * 2.5f);
            vx = (float) (Math.cos(ang) * spd);
            vy = (float) (Math.sin(ang) * spd) - 1.2f;
            maxLife = 20 + (float) (SHARED_RND.nextFloat() * 15);
            life = maxLife;
            color = c;
        }
    }
    private final List<Particle> particles = new ArrayList<>();

    // -------- Botão PULAR --------
    private int botaoPularX, botaoPularY;
    private final int botaoPularLargura = 130;
    private final int botaoPularAltura = 40;
    private boolean botaoPularHover = false;

    // -------- Dots indicator --------
    private final int DOT_SIZE = 10;
    private final int DOT_GAP = 8;

    // -------- Botão "Começar aventura" (último slide) --------
    private boolean botaoIniciarHover = false;
    private int pulseFrame = 0;

    // -------- Fade saída --------
    private boolean emFade = false;
    private int fadeProgresso = 0;
    private final int FADE_MAX = 30;
    private boolean fadeAcaoExecutada = false;

    // -------- Lápis animado (desenho na abertura) --------
    private float pencilX = 0, pencilY = 0;
    private boolean pencilVisible = false;
    private boolean pencilWriting = false;
    private float writingT = 0f;   // 0..1
    private static final float WRITING_SPEED = 0.045f;  // ~22 frames (~0.35s) - rápido e sem travar

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
        this.cenaAtual = 0;
        this.flipState = FlipState.IDLE;
        this.flipT = 0f;
        this.particles.clear();
        this.emFade = false;
        this.fadeProgresso = 0;
        this.fadeAcaoExecutada = false;
        this.pulseFrame = 0;
        this.pencilWriting = true;
        this.writingT = 0f;
        this.pencilVisible = true;

        if (id == 0) {
            nomesImagens = new String[]{
                "slide1.png", "slide2.png", "slide3.png", "slide4.png",
                "slide5.png", "slide6.png", "slide7.png", "slide8.png",
                "slide9.png", "slide10.png", "slide11.png", "slide12.png"
            };
        } else if (id == 1) {
            nomesImagens = new String[]{"slide1.png"};
        } else if (id == 2) {
            nomesImagens = new String[]{"cutscene_final_1.png", "cutscene_final_2.png"};
        }

        imagens = new Image[nomesImagens.length];
        carregarImagens();
    }

    // ======================= FONTES / IMAGENS ==============
    private void carregarFonts() {
        try {
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new File(JogoAudrey.resolvePath("CrayonHandRegular2016Demo.ttf"))).deriveFont(22f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontCrayonHand);
        } catch (Exception e) {
            fontCrayonHand = new Font("Arial", Font.BOLD, 22);
        }
    }

    private void carregarImagens() {
        for (int i = 0; i < nomesImagens.length; i++) {
            String nome = nomesImagens[i];
            String resolved = JogoAudrey.resolvePath(nome);
            java.io.File f = new java.io.File(resolved);
            if (!f.exists()) {
                resolved = JogoAudrey.resolvePath("cutscene/" + nome);
                f = new java.io.File(resolved);
            }
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
        g.setColor(new Color(45, 35, 95));
        g.fillRect(0, 0, 800, 600);
        g.setColor(new Color(150, 130, 230, 80));
        for (int y = 39; y < 600; y += 40) {
            g.drawLine(0, y, 800, y);
        }
        g.setColor(new Color(200, 190, 255));
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
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // 1. Fundo papel bege
        desenharFundoPapel(g2d, w, h);

        // 2. Frame central (moldura do "livro")
        int frameW = Math.min(w - 20, (int) (h * 16.0 / 9.0));
        int frameH = (int) (frameW * 9.0 / 16.0);
        int frameX = (w - frameW) / 2;
        int frameY = (h - frameH) / 2;

        // Sombra da moldura
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRoundRect(frameX + 6, frameY + 10, frameW, frameH, 18, 18);

        // Clip da moldura
        Shape clip = new RoundRectangle2D.Float(frameX, frameY, frameW, frameH, 18, 18);
        g2d.setClip(clip);

        // 3. Desenhar slides
        if (flipState == FlipState.IDLE) {
            desenharSlideEmRect(g2d, cenaAtual, frameX, frameY, frameW, frameH);
        } else {
            desenharSlideTransition(g2d, frameX, frameY, frameW, frameH);
        }

        g2d.setClip(null);

        // 4. Borda da moldura (roxo do menu)
        g2d.setStroke(STROKE_6);
        g2d.setColor(COLOR_MOLDURA_INNER);
        g2d.drawRoundRect(frameX, frameY, frameW, frameH, 18, 18);
        g2d.setStroke(STROKE_8);
        g2d.setColor(COLOR_MOLDURA_OUTER);
        g2d.drawRoundRect(frameX - 1, frameY - 1, frameW + 2, frameH + 2, 20, 20);
        g2d.setStroke(STROKE_1);

        // 5. Partículas sparkle
        desenharParticulas(g2d);

        // 6. Lápis animado
        if (pencilVisible) {
            desenharLapis(g2d);
        }

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

    // ---- Fundo papel (cores do menu principal) --------------
    private void desenharFundoPapel(Graphics2D g2d, int w, int h) {
        GradientPaint bg = new GradientPaint(0, 0, new Color(30, 20, 50), w, h, new Color(20, 10, 40));
        g2d.setPaint(bg);
        g2d.fillRect(0, 0, w, h);

        // Linhas de caderno (lavanda)
        g2d.setColor(new Color(150, 130, 230, 55));
        g2d.setStroke(new BasicStroke(1f));
        for (int y = 40; y < h; y += 40) {
            g2d.drawLine(0, y, w, y);
        }

        // Decorações cantos (estrelas roxas)
        g2d.setColor(new Color(150, 130, 230, 130));
        g2d.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        cutEstrela4(g2d, (int) (w * 0.03), (int) (h * 0.07), 7);
        cutEstrela4(g2d, (int) (w * 0.96), (int) (h * 0.88), 7);
        cutEstrela4(g2d, (int) (w * 0.97), (int) (h * 0.06), 6);
        cutEstrela4(g2d, (int) (w * 0.02), (int) (h * 0.90), 6);

        // Furos de espiral (lado esquerdo)
        g2d.setColor(new Color(0, 0, 0, 150));
        for (int iy = 8; iy < h; iy += 78) {
            g2d.fillOval(6, iy, 14, 14);
        }

        g2d.setStroke(new BasicStroke(1f));
    }

    // ---- Slide preenchendo o retângulo da moldura ---------
    private void desenharSlideEmRect(Graphics2D g2d, int idx, int rx, int ry, int rw, int rh) {
        if (imagens == null || idx < 0 || idx >= imagens.length || imagens[idx] == null) {
            g2d.setColor(new Color(45, 35, 95));
            g2d.fillRect(rx, ry, rw, rh);
            return;
        }
        Image img = imagens[idx];
        int iw = img.getWidth(this);
        int ih = img.getHeight(this);
        if (iw <= 0 || ih <= 0) {
            g2d.setColor(new Color(45, 35, 95));
            g2d.fillRect(rx, ry, rw, rh);
            return;
        }
        double scale = Math.max((double) rw / iw, (double) rh / ih);
        int fw = (int) (iw * scale);
        int fh = (int) (ih * scale);
        int ix = rx + (rw - fw) / 2;
        int iy = ry + (rh - fh) / 2;

        // Reveal diagonal apenas se pencilWriting estiver ativo (abertura do jogo)
        if (pencilWriting && flipState == FlipState.IDLE) {
            float diag = writingT * (rw + rh);
            if (diag <= 0) {
                return;
            }
            Shape clipSalvo = g2d.getClip();
            Path2D.Float clipPath = new Path2D.Float();
            if (diag >= rw + rh) {
                clipPath.moveTo(rx, ry);
                clipPath.lineTo(rx + rw, ry);
                clipPath.lineTo(rx + rw, ry + rh);
                clipPath.lineTo(rx, ry + rh);
            } else {
                clipPath.moveTo(rx, ry);
                clipPath.lineTo(rx + Math.min(diag, rw), ry);
                if (diag > rw) {
                    clipPath.lineTo(rx + rw, ry + (diag - rw));
                }
                if (diag > rh) {
                    clipPath.lineTo(rx + (diag - rh), ry + rh);
                    clipPath.lineTo(rx, ry + rh);
                } else {
                    clipPath.lineTo(rx, ry + diag);
                }
            }
            clipPath.closePath();
            g2d.clip(clipPath);
            g2d.drawImage(img, ix, iy, fw, fh, this);
            g2d.setClip(clipSalvo);
        } else {
            g2d.drawImage(img, ix, iy, fw, fh, this);
        }

        // Animação do botão "Começar aventura" no último slide
        if (idx == imagens.length - 1 && !pencilWriting && flipState == FlipState.IDLE) {
            desenharAnimacaoBotaoIniciar(g2d, ix, iy, fw, fh, scale);
        }
    }

    // ---- Transição Fluida e Rápida entre Slides (Zero Tela Preta) -----
    private void desenharSlideTransition(Graphics2D g2d, int fx, int fy, int fw, int fh) {
        boolean goingNext = (flipState == FlipState.FLIP_NEXT);
        float t = easing(flipT);  // 0..1

        int offsetOut, offsetIn;
        if (goingNext) {
            offsetOut = (int) (-t * fw);
            offsetIn = offsetOut + fw;
        } else {
            offsetOut = (int) (t * fw);
            offsetIn = offsetOut - fw;
        }

        // 1. Desenha o slide de saída
        desenharSlideEmRect(g2d, flipFrom, fx + offsetOut, fy, fw, fh);

        // 2. Desenha o slide de entrada
        desenharSlideEmRect(g2d, flipTo, fx + offsetIn, fy, fw, fh);

        // 3. Sombra suave na borda divisória para dar profundidade
        int shadowW = 35;
        if (goingNext) {
            int shadowX = fx + offsetIn;
            GradientPaint sh = new GradientPaint(
                    shadowX - shadowW, fy, new Color(0, 0, 0, 0),
                    shadowX, fy, new Color(0, 0, 0, 110));
            g2d.setPaint(sh);
            g2d.fillRect(shadowX - shadowW, fy, shadowW, fh);
        } else {
            int shadowX = fx + offsetIn + fw;
            GradientPaint sh = new GradientPaint(
                    shadowX, fy, new Color(0, 0, 0, 110),
                    shadowX + shadowW, fy, new Color(0, 0, 0, 0));
            g2d.setPaint(sh);
            g2d.fillRect(shadowX, fy, shadowW, fh);
        }
    }

    // ---- Animação do botão "Começar aventura" -------------
    private void desenharAnimacaoBotaoIniciar(Graphics2D g2d, int ix, int iy, int fw, int fh, double scale) {
        int btnIX = 541, btnIY = 909, btnIW = 786, btnIH = 78;
        int bx = ix + (int) (btnIX * scale);
        int by = iy + (int) (btnIY * scale);
        int bw = (int) (btnIW * scale);
        int bh = (int) (btnIH * scale);
        if (bw <= 0 || bh <= 0) {
            return;
        }
        int arc = (int) (bh * 0.7);

        float t = pulseFrame * 0.06f;
        float p1 = (float) (Math.sin(t) * 0.5 + 0.5);
        float p2 = (float) (Math.sin(t + 1.2) * 0.5 + 0.5);
        float p3 = (float) (Math.sin(t + 2.4) * 0.5 + 0.5);

        // Camada externa
        int a1 = (int) (40 + p1 * 80);
        g2d.setColor(new Color(75, 65, 135, a1));
        g2d.setStroke(new BasicStroke(4 + p2 * 6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawRoundRect(bx - 8, by - 8, bw + 16, bh + 16, arc + 4, arc + 4);

        // Camada média
        int a2 = (int) (60 + p2 * 100);
        g2d.setColor(new Color(120, 100, 200, a2));
        g2d.setStroke(new BasicStroke(2 + p3 * 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawRoundRect(bx - 4, by - 4, bw + 8, bh + 8, arc, arc);

        // Camada interna
        int a3 = (int) (80 + p1 * 120);
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
            float ang = (float) (t * 0.8 + i * 1.047);
            float dist = 0.7f + (float) (Math.sin(t * 0.5 + i) * 0.3);
            int px = cx + (int) (Math.cos(ang) * raioX * dist);
            int py = cy + (int) (Math.sin(ang) * raioY * dist);
            int sz = (int) (2 + p3 * 3);
            g2d.setColor(new Color(140, 120, 200, (int) (100 + p1 * 120)));
            g2d.fillOval(px - sz / 2, py - sz / 2, sz, sz);
        }
        g2d.setStroke(new BasicStroke(2f));
    }

    // ---- Partículas ----------------------------------------
    private void desenharParticulas(Graphics2D g2d) {
        for (Particle p : particles) {
            float alpha = p.life / p.maxLife;
            int a = (int) (alpha * 200);
            int sz = (int) (3 + alpha * 5);
            g2d.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), Math.min(255, a)));
            g2d.fillOval((int) (p.x - sz / 2f), (int) (p.y - sz / 2f), sz, sz);
        }
    }

    // ---- Lápis animado -------------------------------------
    private void desenharLapis(Graphics2D g2d) {
        int px = (int) pencilX;
        int py = (int) pencilY;
        float inclinacao = (flipState == FlipState.FLIP_NEXT) ? -20f : (flipState == FlipState.FLIP_PREV ? 20f : -10f);
        Graphics2D gp = (Graphics2D) g2d.create();
        gp.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gp.rotate(Math.toRadians(inclinacao), px, py);
        
        // Sombra suave
        gp.setColor(new Color(0, 0, 0, 40));
        gp.fillOval(px - 8, py + 20, 18, 6);
        
        // corpo amarelo
        gp.setColor(new Color(242, 201, 76));
        int[] xp = {px - 6, px + 6, px + 6, px - 6};
        int[] yp = {py - 24, py - 24, py + 14, py + 14};
        gp.fillPolygon(xp, yp, 4);
        
        // linha divisória
        gp.setColor(new Color(200, 160, 40, 160));
        gp.drawLine(px - 6, py - 6, px + 6, py - 6);
        
        // ponta (madeira)
        gp.setColor(new Color(210, 170, 110));
        int[] xp2 = {px - 5, px + 5, px};
        int[] yp2 = {py + 14, py + 14, py + 26};
        gp.fillPolygon(xp2, yp2, 3);
        
        // grafite (ponta escura)
        gp.setColor(new Color(60, 60, 60));
        int[] xp3 = {px - 1, px + 1, px};
        int[] yp3 = {py + 22, py + 22, py + 26};
        gp.fillPolygon(xp3, yp3, 3);
        
        // borracha (topo)
        gp.setColor(new Color(242, 140, 160));
        gp.fillRoundRect(px - 6, py - 28, 12, 8, 4, 4);
        
        // anel metálico
        gp.setColor(new Color(180, 180, 200));
        gp.fillRect(px - 7, py - 22, 14, 4);
        
        // borda
        gp.setColor(new Color(138, 109, 30));
        gp.setStroke(new BasicStroke(1.5f));
        int[] bxp = {px - 6, px + 6, px + 6, px - 6};
        int[] byp = {py - 24, py - 24, py + 14, py + 14};
        gp.drawPolygon(bxp, byp, 4);
        gp.setStroke(new BasicStroke(1f));
        gp.dispose();
    }

    // ---- Dots indicadores ----------------------------------
    private void desenharDots(Graphics2D g2d, int fx, int fy, int fw, int fh) {
        if (imagens == null || imagens.length <= 1) {
            return;
        }
        int n = imagens.length;
        int totalW = n * DOT_SIZE + (n - 1) * DOT_GAP;
        int startX = fx + (fw - totalW) / 2;
        int dotY = fy + fh - 22;
        for (int i = 0; i < n; i++) {
            int dx = startX + i * (DOT_SIZE + DOT_GAP);
            if (i == cenaAtual) {
                g2d.setColor(new Color(150, 130, 230)); // lavanda "on"
                g2d.fillOval(dx - 1, dotY - 1, DOT_SIZE + 2, DOT_SIZE + 2);
                g2d.setColor(new Color(150, 130, 230, 60));
                g2d.fillOval(dx - 4, dotY - 4, DOT_SIZE + 8, DOT_SIZE + 8);
            } else {
                g2d.setColor(new Color(200, 190, 255, 80));
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
        Color corFundo = new Color(45, 35, 95);
        Color corHover = new Color(75, 65, 135);
        Color corTexto = Color.WHITE;

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
        float alpha = 0.55f + (float) (Math.sin(pulseFrame * 0.05) * 0.45);
        g2d.setFont(fontCrayonHand.deriveFont(12f));
        String txt = "clique / \u2192 para avançar";
        FontMetrics fm = g2d.getFontMetrics();
        int tw = fm.stringWidth(txt);
        int tx = w - tw - 24;
        int ty = h - 30;

        g2d.setColor(new Color(255, 255, 255, (int) (alpha * 160)));
        g2d.fillRoundRect(tx - 8, ty - fm.getAscent() - 2, tw + 16, fm.getHeight() + 4, 16, 16);
        g2d.setColor(new Color(200, 190, 255, (int) (alpha * 255)));
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
        // Transição de Slide
        if (flipState != FlipState.IDLE) {
            flipT += FLIP_SPEED;
            if (flipT >= 1f) {
                flipT = 0f;
                cenaAtual = flipTo;
                flipState = FlipState.IDLE;
                
                // Spawn sparkles suaves no final da transição
                int w = getWidth(), h = getHeight();
                for (int i = 0; i < 10; i++) {
                    particles.add(new Particle(
                            w / 2f + (SHARED_RND.nextFloat() - 0.5f) * 300,
                            h / 2f + (SHARED_RND.nextFloat() - 0.5f) * 150,
                            SPARK_COLORS[SHARED_RND.nextInt(SPARK_COLORS.length)]));
                }
            }
        }

        // Partículas
        particles.removeIf(p -> {
            p.x += p.vx;
            p.y += p.vy;
            p.vy += 0.05f;
            p.life--;
            return p.life <= 0;
        });

        // Lápis desenhando no início (rápido e fluido)
        if (pencilWriting) {
            writingT += WRITING_SPEED;
            if (writingT >= 1f) {
                writingT = 1f;
                pencilWriting = false;
                pencilVisible = false;
            } else {
                pencilVisible = true;
                int w2 = getWidth(), h2 = getHeight();
                int frameW2 = Math.min(w2 - 20, (int) (h2 * 16.0 / 9.0));
                int frameH2 = (int) (frameW2 * 9.0 / 16.0);
                int frameX2 = (w2 - frameW2) / 2;
                int frameY2 = (h2 - frameH2) / 2;
                
                pencilX = frameX2 + writingT * frameW2;
                pencilY = frameY2 + writingT * frameH2;
                if (SHARED_RND.nextInt(2) == 0) {
                    particles.add(new Particle(pencilX, pencilY, SPARK_COLORS[SHARED_RND.nextInt(SPARK_COLORS.length)]));
                }
            }
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
        if (flipState != FlipState.IDLE || emFade) {
            return;
        }
        if (imagens != null && cenaAtual < imagens.length - 1) {
            GerenciadorAudio.tocarSomDialogo();
            flipFrom = cenaAtual;
            flipTo = cenaAtual + 1;
            flipT = 0f;
            flipState = FlipState.FLIP_NEXT;
            pencilWriting = false;
            pencilVisible = false;
        } else {
            disparararFade();
        }
    }

    private void voltarSlide() {
        if (flipState != FlipState.IDLE || emFade) {
            return;
        }
        if (cenaAtual > 0) {
            GerenciadorAudio.tocarSomDialogo();
            flipFrom = cenaAtual;
            flipTo = cenaAtual - 1;
            flipT = 0f;
            flipState = FlipState.FLIP_PREV;
            pencilWriting = false;
            pencilVisible = false;
        }
    }

    private void disparararFade() {
        if (!emFade) {
            emFade = true;
            fadeProgresso = 0;
            fadeAcaoExecutada = false;
        }
        pencilWriting = false;
        pencilVisible = false;
    }

    private void iniciarJogo() {
        frame.voltarDeCutscene();
    }

    // ======================= HIT TEST (botão iniciar) =======
    private boolean isNoBotaoIniciar(int mx, int my) {
        if (imagens == null || cenaAtual != imagens.length - 1) {
            return false;
        }
        Image img = imagens[cenaAtual];
        int iw = img.getWidth(this), ih = img.getHeight(this);
        if (iw <= 0 || ih <= 0) {
            return false;
        }
        int w = getWidth(), h = getHeight();
        int frameW = Math.min(w - 20, (int) (h * 16.0 / 9.0));
        int frameH = (int) (frameW * 9.0 / 16.0);
        int frameX = (w - frameW) / 2, frameY = (h - frameH) / 2;
        double scale = Math.max((double) frameW / iw, (double) frameH / ih);
        int fw = (int) (iw * scale), fh = (int) (ih * scale);
        int ix = frameX + (frameW - fw) / 2, iy = frameY + (frameH - fh) / 2;
        int bx = ix + (int) (541 * scale), by = iy + (int) (909 * scale);
        int bw = (int) (786 * scale), bh = (int) (78 * scale);
        return mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
    }

    // ======================= MOUSE =========================
    @Override
    public void mousePressed(MouseEvent e) {
        if (emFade || flipState != FlipState.IDLE) {
            return;
        }
        int mx = e.getX(), my = e.getY();

        // Botão PULAR
        if (mx >= botaoPularX && mx <= botaoPularX + botaoPularLargura
                && my >= botaoPularY && my <= botaoPularY + botaoPularAltura) {
            disparararFade();
            return;
        }

        // Último slide: apenas botão "Começar"
        if (imagens != null && cenaAtual == imagens.length - 1) {
            if (isNoBotaoIniciar(mx, my)) {
                disparararFade();
            }
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

        boolean hp = mx >= botaoPularX && mx <= botaoPularX + botaoPularLargura
                && my >= botaoPularY && my <= botaoPularY + botaoPularAltura;
        boolean hi = isNoBotaoIniciar(mx, my);

        if (hp != botaoPularHover || hi != botaoIniciarHover) {
            botaoPularHover = hp;
            botaoIniciarHover = hi;
            setCursor(new Cursor((hp || hi) ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            repaint();
        }

        if (pencilVisible && flipState == FlipState.IDLE && !pencilWriting) {
            pencilX = mx;
            pencilY = my;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (botaoPularHover || botaoIniciarHover) {
            botaoPularHover = false;
            botaoIniciarHover = false;
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

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
            if (op == JOptionPane.YES_OPTION) {
                frame.voltarAoMenuPrincipal();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
