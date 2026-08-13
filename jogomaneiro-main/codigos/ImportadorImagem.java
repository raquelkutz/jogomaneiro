
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.geom.*;

/**
 * Tela de importacao de imagem para o desenho da missao.
 * Aparece quando o jogador vai entregar o desenho para o grupo.
 */
public class ImportadorImagem extends JPanel implements ActionListener {

    private static final int LARGURA = 1000;
    private static final int ALTURA  = 750;

    private final JogoAudrey frame;
    private final Runnable aoTerminar;

    // Animacao
    private Timer timerAnim;
    private float anguloAnim = 0f;
    private float pulsacao    = 0f;
    private long startTime = System.currentTimeMillis();

    // Imagem pre-visualizacao
    private BufferedImage previewImg = null;
    private String caminhoSelecionado = null;

    // Fontes
    private Font fonteTitulo, fonteCrayon;

    // Botoes
    private JButton btnEscolher, btnConfirmar, btnPular;

    // Destino da imagem
    public static final String NOME_ARQUIVO_DESENHO = "desenho_jogador.png";

    // Particulas flutuantes pre-calculadas
    private static final int NUM_PARTICLES = 90;
    private final float[] particleX = new float[NUM_PARTICLES];
    private final float[] particleY = new float[NUM_PARTICLES];
    private final float[] particleSpeed = new float[NUM_PARTICLES];
    private final float[] particleSize = new float[NUM_PARTICLES];
    private final float[] particlePhase = new float[NUM_PARTICLES];
    private final int[] particleAlpha = new int[NUM_PARTICLES];

    public ImportadorImagem(JogoAudrey frame, Runnable aoTerminar) {
        this.frame = frame;
        this.aoTerminar = aoTerminar;

        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setLayout(null);
        setBackground(new Color(18, 10, 48));

        // Inicializar particulas
        for (int i = 0; i < NUM_PARTICLES; i++) {
            particleX[i] = (float) (Math.random() * LARGURA);
            particleY[i] = (float) (Math.random() * ALTURA);
            particleSpeed[i] = 0.2f + (float) (Math.random() * 0.8f);
            particleSize[i] = 1.5f + (float) (Math.random() * 3.5f);
            particlePhase[i] = (float) (Math.random() * Math.PI * 2);
            particleAlpha[i] = 40 + (int) (Math.random() * 160);
        }

        carregarFontes();
        criarBotoes();

        timerAnim = new Timer(16, this);
        timerAnim.start();
    }

    // Fontes

    private void carregarFontes() {
        try {
            fonteTitulo = Font.createFont(Font.TRUETYPE_FONT,
                    new File(JogoAudrey.resolvePath("KGSecondChancesSketch.ttf"))).deriveFont(56f);
            fonteCrayon = Font.createFont(Font.TRUETYPE_FONT,
                    new File(JogoAudrey.resolvePath("CrayonHandRegular2016Demo.ttf"))).deriveFont(22f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fonteTitulo);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fonteCrayon);
        } catch (Exception e) {
            fonteTitulo = new Font("Arial", Font.BOLD, 56);
            fonteCrayon = new Font("Arial", Font.PLAIN, 22);
        }
    }

    // Botoes

    private void criarBotoes() {
        btnEscolher = criarBotao("\u2728  Escolher Imagem do Dispositivo",
                new Color(110, 60, 200), new Color(150, 90, 255), true);
        btnEscolher.setEnabled(true);
        btnEscolher.addActionListener(e -> escolherImagem());
        add(btnEscolher);

        btnConfirmar = criarBotao("\u2705  Usar esta Imagem",
                new Color(45, 140, 80), new Color(65, 180, 100), true);
        btnConfirmar.setEnabled(false);
        btnConfirmar.addActionListener(e -> confirmarImagem());
        add(btnConfirmar);

        btnPular = criarBotao("\u25B6  Pular (usar imagem padrao)",
                new Color(65, 55, 100), new Color(90, 75, 130), false);
        btnPular.addActionListener(e -> pular());
        add(btnPular);
    }

    private JButton criarBotao(String texto, Color corBase, Color corHover, boolean isPrimary) {
        JButton btn = new JButton(texto) {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth(), h = getHeight();
                int radius = 24;

                // Sombra
                if (isEnabled() && isPrimary) {
                    g2.setColor(new Color(0, 0, 0, hover ? 60 : 30));
                    g2.fillRoundRect(2, 4, w - 2, h - 2, radius, radius);
                }

                // Fundo com gradiente
                Color c = isEnabled() ? (hover ? corHover : corBase) : new Color(50, 45, 70);
                GradientPaint gp = new GradientPaint(0, 0, c, 0, h, c.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, radius, radius);

                // Brilho superior (glassmorphism)
                if (isEnabled()) {
                    GradientPaint shine = new GradientPaint(0, 0, new Color(255, 255, 255, hover ? 70 : 35),
                            0, h / 2, new Color(255, 255, 255, 0));
                    g2.setPaint(shine);
                    g2.fillRoundRect(0, 0, w, h / 2, radius, radius);
                }

                // Borda
                g2.setColor(new Color(255, 255, 255, hover ? 100 : 30));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);

                // Glow externo ao hover
                if (isEnabled() && hover && isPrimary) {
                    g2.setColor(new Color(corHover.getRed(), corHover.getGreen(), corHover.getBlue(), 40));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawRoundRect(-2, -2, w + 3, h + 3, radius + 4, radius + 4);
                }

                // Texto
                g2.setColor(isEnabled() ? Color.WHITE : new Color(130, 120, 160));
                Font f = fonteCrayon != null ? fonteCrayon.deriveFont(isPrimary ? 20f : 17f) : new Font("Arial", Font.BOLD, isPrimary ? 20 : 17);
                g2.setFont(f);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (w - fm.stringWidth(getText())) / 2,
                        (h + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Logica

    private void escolherImagem() {
        Window window = SwingUtilities.getWindowAncestor(this);
        Frame frameParent = window instanceof Frame ? (Frame) window : null;
        
        // Usa o dialog nativo moderno do sistema operacional
        FileDialog dialog = new FileDialog(frameParent, "Selecione o seu desenho", FileDialog.LOAD);
        dialog.setFile("*.png;*.jpg;*.jpeg");
        dialog.setVisible(true);

        String arquivo = dialog.getFile();
        String diretorio = dialog.getDirectory();

        if (arquivo != null && diretorio != null) {
            File selectedFile = new File(diretorio, arquivo);
            try {
                previewImg = ImageIO.read(selectedFile);
                if (previewImg == null) {
                    throw new IOException("Formato de imagem nao suportado");
                }
                caminhoSelecionado = selectedFile.getAbsolutePath();
                btnConfirmar.setEnabled(true);
                repaint();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Nao foi possivel carregar a imagem.\nTente um arquivo PNG ou JPG valido.",
                        "Erro ao carregar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void confirmarImagem() {
        if (caminhoSelecionado == null) return;
        try {
            File destino = new File(JogoAudrey.resolvePath(NOME_ARQUIVO_DESENHO));
            BufferedImage img = ImageIO.read(new File(caminhoSelecionado));
            ImageIO.write(img, "png", destino);
            System.out.println("[IMPORTADOR] Imagem salva em: " + destino.getAbsolutePath());
            encerrar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao salvar a imagem:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pular() {
        System.out.println("[IMPORTADOR] Usuario pulou a importacao de imagem.");
        encerrar();
    }

    private void encerrar() {
        timerAnim.stop();
        if (aoTerminar != null) aoTerminar.run();
    }

    // Animacao

    @Override
    public void actionPerformed(ActionEvent e) {
        anguloAnim += 0.03f;
        pulsacao    += 0.05f;
        repaint();
    }

    // Pintura

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int W = getWidth(), H = getHeight();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        float pulse = 0.5f + 0.5f * (float) Math.sin(pulsacao);
        float time = (System.currentTimeMillis() - startTime) / 1000f;

        // ===== FUNDO GRADIENTE RADIAL PROFUNDO =====
        GradientPaint bg = new GradientPaint(0, 0, new Color(15, 8, 45), W, H, new Color(40, 15, 75));
        g2.setPaint(bg);
        g2.fillRect(0, 0, W, H);

        // Nebulosa roxa suave (radial center)
        RadialGradientPaint nebula = new RadialGradientPaint(
                W * 0.5f, H * 0.35f, W * 0.6f,
                new float[]{0f, 0.5f, 1f},
                new Color[]{new Color(100, 40, 180, 50), new Color(60, 20, 120, 25), new Color(0, 0, 0, 0)});
        g2.setPaint(nebula);
        g2.fillRect(0, 0, W, H);

        // Segundo glow — canto inferior esquerdo
        RadialGradientPaint nebula2 = new RadialGradientPaint(
                W * 0.15f, H * 0.85f, W * 0.4f,
                new float[]{0f, 1f},
                new Color[]{new Color(80, 30, 160, 35), new Color(0, 0, 0, 0)});
        g2.setPaint(nebula2);
        g2.fillRect(0, 0, W, H);

        // Terceiro glow — canto superior direito
        RadialGradientPaint nebula3 = new RadialGradientPaint(
                W * 0.85f, H * 0.15f, W * 0.35f,
                new float[]{0f, 1f},
                new Color[]{new Color(120, 50, 200, 25), new Color(0, 0, 0, 0)});
        g2.setPaint(nebula3);
        g2.fillRect(0, 0, W, H);

        // ===== PARTICULAS FLUTUANTES COM PARALLAX =====
        for (int i = 0; i < NUM_PARTICLES; i++) {
            float px = particleX[i] + (float) Math.sin(time * particleSpeed[i] * 0.4 + particlePhase[i]) * 25;
            float py = particleY[i] + (float) Math.cos(time * particleSpeed[i] * 0.3 + particlePhase[i]) * 18;

            // Wrap around
            px = ((px % W) + W) % W;
            py = ((py % H) + H) % H;

            float alphaMod = 0.6f + 0.4f * (float) Math.sin(time * 1.5 + particlePhase[i]);
            int alpha = Math.min(255, (int) (particleAlpha[i] * alphaMod));
            float sz = particleSize[i];

            if (i % 5 == 0) {
                // Particulas douradas (raras e maiores)
                g2.setColor(new Color(255, 220, 120, alpha / 2));
                g2.fill(new Ellipse2D.Float(px - sz, py - sz, sz * 2.5f, sz * 2.5f));
                g2.setColor(new Color(255, 240, 170, alpha));
                g2.fill(new Ellipse2D.Float(px - sz * 0.5f, py - sz * 0.5f, sz * 1.2f, sz * 1.2f));
            } else {
                // Particulas roxas/lilas
                int r = 150 + (i % 3) * 30;
                int gb = 100 + (i % 4) * 25;
                g2.setColor(new Color(r, gb, 255, alpha));
                g2.fill(new Ellipse2D.Float(px, py, sz, sz));
            }
        }

        // ===== LINHAS DECORATIVAS LATERAIS =====
        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
        g2.setColor(new Color(180, 130, 255));
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i < 8; i++) {
            float offset = (float) Math.sin(time * 0.3 + i * 0.5) * 10;
            g2.drawLine(30 + i * 3, 120 + (int) offset, 30 + i * 3, H - 120 + (int) offset);
            g2.drawLine(W - 30 - i * 3, 120 - (int) offset, W - 30 - i * 3, H - 120 - (int) offset);
        }
        g2.setComposite(oldComposite);

        // ===== TITULO COM SOMBRA E GRADIENTE =====
        g2.setFont(fonteTitulo.deriveFont(58f));
        String titulo = "HOBBY QUEST";
        FontMetrics fmT = g2.getFontMetrics();
        int tx = (W - fmT.stringWidth(titulo)) / 2;
        int ty = 82;

        // Glow atras do titulo
        g2.setColor(new Color(120, 60, 220, (int)(40 + pulse * 30)));
        for (int i = 3; i >= 1; i--) {
            g2.drawString(titulo, tx + i, ty + i);
            g2.drawString(titulo, tx - i, ty + i);
        }

        // Sombra do titulo
        g2.setColor(new Color(20, 10, 50, 180));
        g2.drawString(titulo, tx + 3, ty + 3);

        // Titulo com gradiente
        GradientPaint tituloGrad = new GradientPaint(
                tx, ty - 40, new Color(240, 200, 255),
                tx + fmT.stringWidth(titulo), ty, new Color(180, 120, 255));
        g2.setPaint(tituloGrad);
        g2.drawString(titulo, tx, ty);

        // Linha decorativa sob o titulo
        int lineW = fmT.stringWidth(titulo) + 40;
        int lineX = (W - lineW) / 2;
        GradientPaint lineGrad = new GradientPaint(
                lineX, 0, new Color(180, 120, 255, 0),
                W / 2f, 0, new Color(180, 120, 255, 100));
        g2.setPaint(lineGrad);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(lineX, ty + 12, W / 2, ty + 12);
        GradientPaint lineGrad2 = new GradientPaint(
                W / 2f, 0, new Color(180, 120, 255, 100),
                lineX + lineW, 0, new Color(180, 120, 255, 0));
        g2.setPaint(lineGrad2);
        g2.drawLine(W / 2, ty + 12, lineX + lineW, ty + 12);

        // Diamante central na linha
        int dCx = W / 2, dCy = ty + 12;
        g2.setColor(new Color(220, 180, 255, (int)(150 + pulse * 80)));
        int[] dx = {dCx, dCx + 5, dCx, dCx - 5};
        int[] dy = {dCy - 5, dCy, dCy + 5, dCy};
        g2.fillPolygon(dx, dy, 4);

        // ===== SUBTITULO =====
        Font subFont = (fonteCrayon != null ? fonteCrayon : new Font("Arial", Font.PLAIN, 22)).deriveFont(24f);
        g2.setFont(subFont);
        String sub = "\u2728 Envie o seu Desenho! \u2728";
        FontMetrics fmS = g2.getFontMetrics();
        g2.setColor(new Color(220, 195, 255, 220));
        g2.drawString(sub, (W - fmS.stringWidth(sub)) / 2, ty + 48);

        // ===== CAIXA DE INSTRUCAO GLASSMORPHISM =====
        int boxW = 500, boxH = 80;
        int boxX = (W - boxW) / 2, boxY = ty + 65;
        
        // Fundo glass
        g2.setColor(new Color(255, 255, 255, 8));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 20, 20);
        
        // Borda sutil
        g2.setColor(new Color(200, 170, 255, 40));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 20, 20);

        // Icone de pincel
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        g2.drawString("\uD83C\uDFA8", boxX + 18, boxY + 35);

        g2.setFont(subFont.deriveFont(16f));
        FontMetrics fmI = g2.getFontMetrics();
        String[] linhas = {
            "Selecione a foto ou arquivo do desenho",
            "que voce fez sobre o tema 'Solidao Urbana'!"
        };
        g2.setColor(new Color(225, 205, 255, 230));
        for (int i = 0; i < linhas.length; i++) {
            g2.drawString(linhas[i], (W - fmI.stringWidth(linhas[i])) / 2 + 10, boxY + 30 + i * 24);
        }

        // ===== AREA DE PREVIEW PREMIUM =====
        int prevW = 260, prevH = 220;
        int prevX = (W - prevW) / 2, prevY = boxY + boxH + 25;

        // Sombra difusa do preview
        for (int i = 4; i >= 1; i--) {
            g2.setColor(new Color(80, 30, 160, 8 * i));
            g2.fillRoundRect(prevX - i * 3, prevY - i * 3, prevW + i * 6, prevH + i * 6, 28, 28);
        }

        // Fundo do preview
        g2.setColor(new Color(30, 18, 60, 200));
        g2.fillRoundRect(prevX, prevY, prevW, prevH, 20, 20);

        // Borda animada com gradiente rotativo
        float angle = anguloAnim * 2;
        Color borderColor1 = new Color(
                (int)(160 + 60 * Math.sin(angle)),
                (int)(100 + 40 * Math.sin(angle + 1)),
                255, (int)(150 + pulse * 80));
        Color borderColor2 = new Color(
                (int)(200 + 40 * Math.sin(angle + 2)),
                (int)(120 + 50 * Math.sin(angle + 3)),
                (int)(220 + 35 * Math.cos(angle)), (int)(120 + pulse * 60));
        GradientPaint borderGrad = new GradientPaint(
                prevX, prevY, borderColor1, prevX + prevW, prevY + prevH, borderColor2);
        g2.setPaint(borderGrad);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(prevX, prevY, prevW, prevH, 20, 20);

        // Cantos brilhantes
        int cornerSize = 12;
        g2.setColor(new Color(200, 160, 255, (int)(100 + pulse * 100)));
        g2.setStroke(new BasicStroke(2f));
        // Top-left
        g2.drawLine(prevX + 6, prevY + 6, prevX + 6 + cornerSize, prevY + 6);
        g2.drawLine(prevX + 6, prevY + 6, prevX + 6, prevY + 6 + cornerSize);
        // Top-right
        g2.drawLine(prevX + prevW - 6 - cornerSize, prevY + 6, prevX + prevW - 6, prevY + 6);
        g2.drawLine(prevX + prevW - 6, prevY + 6, prevX + prevW - 6, prevY + 6 + cornerSize);
        // Bottom-left
        g2.drawLine(prevX + 6, prevY + prevH - 6, prevX + 6 + cornerSize, prevY + prevH - 6);
        g2.drawLine(prevX + 6, prevY + prevH - 6 - cornerSize, prevX + 6, prevY + prevH - 6);
        // Bottom-right
        g2.drawLine(prevX + prevW - 6 - cornerSize, prevY + prevH - 6, prevX + prevW - 6, prevY + prevH - 6);
        g2.drawLine(prevX + prevW - 6, prevY + prevH - 6 - cornerSize, prevX + prevW - 6, prevY + prevH - 6);

        if (previewImg != null) {
            // Desenhar imagem com margem interna
            int margin = 10;
            int imgX = prevX + margin, imgY = prevY + margin;
            int imgW = prevW - margin * 2, imgH = prevH - margin * 2;

            // Clip arredondado
            Shape oldClip = g2.getClip();
            g2.setClip(new RoundRectangle2D.Float(imgX, imgY, imgW, imgH, 12, 12));
            g2.drawImage(previewImg, imgX, imgY, imgW, imgH, this);
            g2.setClip(oldClip);

            // Overlay de reflexo
            GradientPaint imgShine = new GradientPaint(
                    imgX, imgY, new Color(255, 255, 255, 25),
                    imgX, imgY + imgH / 3, new Color(255, 255, 255, 0));
            g2.setPaint(imgShine);
            g2.fill(new RoundRectangle2D.Float(imgX, imgY, imgW, imgH / 3, 12, 12));

            // Badge "Imagem selecionada"
            g2.setFont(subFont.deriveFont(13f));
            String badge = "\u2705 Imagem carregada";
            FontMetrics fmB = g2.getFontMetrics();
            int badgeW = fmB.stringWidth(badge) + 16;
            int badgeH = 24;
            int badgeX = prevX + (prevW - badgeW) / 2;
            int badgeY = prevY + prevH + 8;
            g2.setColor(new Color(40, 160, 80, 180));
            g2.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 12, 12);
            g2.setColor(new Color(200, 255, 200));
            g2.drawString(badge, badgeX + 8, badgeY + 17);
        } else {
            // Placeholder elegante
            g2.setColor(new Color(80, 50, 140, 60));
            g2.fillRoundRect(prevX + 8, prevY + 8, prevW - 16, prevH - 16, 14, 14);

            // Icone de imagem centralizado
            int iconSize = 70;
            int iconX = prevX + (prevW - iconSize) / 2;
            int iconY = prevY + (prevH - iconSize) / 2 - 15;

            // Frame do icone
            g2.setColor(new Color(150, 110, 220, (int)(80 + pulse * 50)));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRoundRect(iconX, iconY, iconSize, iconSize, 10, 10);

            // Montanha
            int[] mx = {iconX + 10, iconX + iconSize / 2, iconX + iconSize - 10};
            int[] my = {iconY + iconSize - 15, iconY + 30, iconY + iconSize - 15};
            g2.setColor(new Color(140, 100, 220, 100));
            g2.fillPolygon(mx, my, 3);

            // Sol
            g2.setColor(new Color(255, 210, 100, (int)(120 + pulse * 80)));
            g2.fillOval(iconX + iconSize - 30, iconY + 12, 20, 20);

            // Texto placeholder
            g2.setFont(subFont.deriveFont(15f));
            FontMetrics fmP = g2.getFontMetrics();
            String ph = "Arraste ou selecione sua imagem";
            g2.setColor(new Color(180, 150, 230, 160));
            g2.drawString(ph, prevX + (prevW - fmP.stringWidth(ph)) / 2, prevY + prevH - 25);

            // Seta animada para baixo (aponta para o botao)
            int arrowX = W / 2;
            int arrowY = prevY + prevH + 18 + (int)(Math.sin(time * 3) * 4);
            g2.setColor(new Color(180, 140, 255, (int)(120 + pulse * 80)));
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(arrowX, arrowY, arrowX - 6, arrowY - 8);
            g2.drawLine(arrowX, arrowY, arrowX + 6, arrowY - 8);
        }

        // ===== REPOSICIONAR BOTOES =====
        int btnW = Math.min(420, W - 120), btnH = 50;
        int cx = (W - btnW) / 2;
        int btnStartY = prevY + prevH + 35;

        btnEscolher.setBounds(cx, btnStartY, btnW, btnH);
        btnConfirmar.setBounds(cx, btnStartY + 60, btnW, btnH);
        btnPular.setBounds(cx + 30, btnStartY + 122, btnW - 60, 36);

        // ===== RODAPE SUTIL =====
        g2.setFont(subFont.deriveFont(11f));
        g2.setColor(new Color(140, 120, 180, 80));
        String footer = "Formatos aceitos: PNG, JPG";
        FontMetrics fmF = g2.getFontMetrics();
        g2.drawString(footer, (W - fmF.stringWidth(footer)) / 2, H - 15);
    }

    // Verificacao estatica

    /**
     * Retorna true se o jogo deve mostrar a tela de importacao.
     * Mostra quando a imagem da Gabi ainda e o placeholder (muito pequena).
     */
    public static boolean deveImportar() {
        return true;
    }
}
