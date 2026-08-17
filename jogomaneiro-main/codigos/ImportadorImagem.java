import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.geom.*;
import java.util.List;

/**
 * Tela de importação de imagem para o desenho da missão.
 * Suporta seleção de arquivos (PNG, JPG, BMP, etc.) e Arrastar-e-Soltar (Drag and Drop).
 */
public class ImportadorImagem extends JPanel implements ActionListener {

    private static final int LARGURA = 1000;
    private static final int ALTURA  = 750;

    private final JogoAudrey frame;
    private final Runnable aoTerminar;

    // Animação
    private Timer timerAnim;
    private float anguloAnim = 0f;
    private float pulsacao    = 0f;
    private long startTime = System.currentTimeMillis();

    // Imagem pré-visualização
    private BufferedImage previewImg = null;
    private String caminhoSelecionado = null;

    // Fontes
    private Font fonteTitulo, fonteCrayon;

    // Botões
    private JButton btnEscolher, btnConfirmar, btnPular;

    // Destino da imagem
    public static final String NOME_ARQUIVO_DESENHO = "desenho_jogador.png";

    // Partículas flutuantes
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

        // Inicializar partículas
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
        configurarDragAndDrop();

        timerAnim = new Timer(16, this);
        timerAnim.start();
    }

    // Suporte a Arrastar e Soltar imagem direto na tela
    private void configurarDragAndDrop() {
        new DropTarget(this, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable tr = dtde.getTransferable();
                    if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        @SuppressWarnings("unchecked")
                        List<File> files = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                        if (files != null && !files.isEmpty()) {
                            carregarArquivo(files.get(0));
                            dtde.dropComplete(true);
                            return;
                        }
                    }
                    dtde.dropComplete(false);
                } catch (Exception ex) {
                    dtde.dropComplete(false);
                }
            }
        });
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

    // Botões
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

        btnPular = criarBotao("\u25B6  Pular (usar imagem padrão)",
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

                // Brilho superior
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

    // Lógica de seleção e carregamento de imagem
    private void escolherImagem() {
        try {
            // Usa o diálogo nativo moderno do Windows (Windows Explorer)
            Window window = SwingUtilities.getWindowAncestor(this);
            Frame frameParent = window instanceof Frame ? (Frame) window : null;
            FileDialog dialog = new FileDialog(frameParent, "Selecione o seu desenho", FileDialog.LOAD);
            
            // Filtro de extensões limpo
            dialog.setFilenameFilter((dir, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                        || lower.endsWith(".bmp") || lower.endsWith(".gif") || lower.endsWith(".webp");
            });

            dialog.setVisible(true);

            String arquivo = dialog.getFile();
            String diretorio = dialog.getDirectory();

            if (arquivo != null && diretorio != null) {
                carregarArquivo(new File(diretorio, arquivo));
                return;
            }
        } catch (Exception e) {
            // Fallback usando JFileChooser com o Look and Feel nativo do Windows
            try {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}

                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Selecione o seu desenho");
                chooser.setAcceptAllFileFilterUsed(true);
                chooser.setFileFilter(new FileNameExtensionFilter(
                        "Imagens (*.png, *.jpg, *.jpeg, *.bmp, *.gif, *.webp)",
                        "png", "jpg", "jpeg", "bmp", "gif", "webp", "PNG", "JPG", "JPEG"));

                File userHome = new File(System.getProperty("user.home"));
                File pictures = new File(userHome, "Pictures");
                if (pictures.exists()) {
                    chooser.setCurrentDirectory(pictures);
                } else {
                    File downloads = new File(userHome, "Downloads");
                    if (downloads.exists()) chooser.setCurrentDirectory(downloads);
                }

                int res = chooser.showOpenDialog(this);
                if (res == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = chooser.getSelectedFile();
                    carregarArquivo(selectedFile);
                }
            } catch (Exception ex2) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao abrir seletor de arquivos: " + ex2.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void carregarArquivo(File selectedFile) {
        if (selectedFile == null || !selectedFile.exists()) return;
        try {
            BufferedImage img = ImageIO.read(selectedFile);
            if (img == null) {
                JOptionPane.showMessageDialog(this,
                        "Formato de imagem não suportado.\nPor favor, selecione um arquivo PNG ou JPG válido.",
                        "Formato Inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }
            previewImg = img;
            caminhoSelecionado = selectedFile.getAbsolutePath();
            btnConfirmar.setEnabled(true);
            repaint();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Não foi possível carregar a imagem:\n" + ex.getMessage(),
                    "Erro ao Carregar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void confirmarImagem() {
        if (previewImg == null && caminhoSelecionado == null) return;
        try {
            String resolved = JogoAudrey.resolvePath(NOME_ARQUIVO_DESENHO);
            File destino = new File(resolved != null ? resolved : NOME_ARQUIVO_DESENHO);
            if (destino.getParentFile() != null && !destino.getParentFile().exists()) {
                destino.getParentFile().mkdirs();
            }

            BufferedImage imgParaSalvar = previewImg;
            if (imgParaSalvar == null && caminhoSelecionado != null) {
                imgParaSalvar = ImageIO.read(new File(caminhoSelecionado));
            }

            if (imgParaSalvar != null) {
                ImageIO.write(imgParaSalvar, "png", destino);
                // Também garante cópia direta na raiz de execução
                File raiz = new File(NOME_ARQUIVO_DESENHO);
                if (!raiz.getAbsolutePath().equals(destino.getAbsolutePath())) {
                    ImageIO.write(imgParaSalvar, "png", raiz);
                }
            }
            System.out.println("[IMPORTADOR] Imagem salva com sucesso em: " + destino.getAbsolutePath());
            encerrar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao salvar a imagem:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pular() {
        System.out.println("[IMPORTADOR] Usuário pulou a importação de imagem.");
        encerrar();
    }

    private void encerrar() {
        timerAnim.stop();
        if (aoTerminar != null) aoTerminar.run();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        anguloAnim += 0.03f;
        pulsacao    += 0.05f;
        repaint();
    }

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

        // Fundo gradiente profundo
        GradientPaint bg = new GradientPaint(0, 0, new Color(15, 8, 45), W, H, new Color(40, 15, 75));
        g2.setPaint(bg);
        g2.fillRect(0, 0, W, H);

        // Nebulosa central
        RadialGradientPaint nebula = new RadialGradientPaint(
                W * 0.5f, H * 0.35f, W * 0.6f,
                new float[]{0f, 0.5f, 1f},
                new Color[]{new Color(100, 40, 180, 50), new Color(60, 20, 120, 25), new Color(0, 0, 0, 0)});
        g2.setPaint(nebula);
        g2.fillRect(0, 0, W, H);

        // Partículas flutuantes
        for (int i = 0; i < NUM_PARTICLES; i++) {
            float px = particleX[i] + (float) Math.sin(time * particleSpeed[i] * 0.4 + particlePhase[i]) * 25;
            float py = particleY[i] + (float) Math.cos(time * particleSpeed[i] * 0.3 + particlePhase[i]) * 18;

            px = ((px % W) + W) % W;
            py = ((py % H) + H) % H;

            float alphaMod = 0.6f + 0.4f * (float) Math.sin(time * 1.5 + particlePhase[i]);
            int alpha = Math.min(255, (int) (particleAlpha[i] * alphaMod));
            float sz = particleSize[i];

            if (i % 5 == 0) {
                g2.setColor(new Color(255, 220, 120, alpha / 2));
                g2.fill(new Ellipse2D.Float(px - sz, py - sz, sz * 2.5f, sz * 2.5f));
                g2.setColor(new Color(255, 240, 170, alpha));
                g2.fill(new Ellipse2D.Float(px - sz * 0.5f, py - sz * 0.5f, sz * 1.2f, sz * 1.2f));
            } else {
                int r = 150 + (i % 3) * 30;
                int gb = 100 + (i % 4) * 25;
                g2.setColor(new Color(r, gb, 255, alpha));
                g2.fill(new Ellipse2D.Float(px, py, sz, sz));
            }
        }

        // Título
        g2.setFont(fonteTitulo.deriveFont(58f));
        String titulo = "HOBBY QUEST";
        FontMetrics fmT = g2.getFontMetrics();
        int tx = (W - fmT.stringWidth(titulo)) / 2;
        int ty = 82;

        g2.setColor(new Color(120, 60, 220, (int)(40 + pulse * 30)));
        for (int i = 3; i >= 1; i--) {
            g2.drawString(titulo, tx + i, ty + i);
            g2.drawString(titulo, tx - i, ty + i);
        }

        g2.setColor(new Color(20, 10, 50, 180));
        g2.drawString(titulo, tx + 3, ty + 3);

        GradientPaint tituloGrad = new GradientPaint(
                tx, ty - 40, new Color(240, 200, 255),
                tx + fmT.stringWidth(titulo), ty, new Color(180, 130, 255));
        g2.setPaint(tituloGrad);
        g2.drawString(titulo, tx, ty);

        // Subtítulo
        Font subFont = fonteCrayon != null ? fonteCrayon : new Font("Arial", Font.PLAIN, 20);
        g2.setFont(subFont.deriveFont(20f));
        String subtitulo = "Entrega do Desenho - Missão 'Solidão Urbana'";
        FontMetrics fmS = g2.getFontMetrics();
        int sx = (W - fmS.stringWidth(subtitulo)) / 2;
        int sy = ty + 38;

        g2.setColor(new Color(255, 235, 180, 230));
        g2.drawString(subtitulo, sx, sy);

        // Box informativa
        int boxW = Math.min(620, W - 80), boxH = 55;
        int boxX = (W - boxW) / 2, boxY = sy + 18;

        g2.setColor(new Color(60, 30, 110, 120));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 16, 16);
        g2.setColor(new Color(160, 120, 240, 80));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 16, 16);

        g2.setFont(subFont.deriveFont(16f));
        FontMetrics fmI = g2.getFontMetrics();
        String[] linhas = {
            "Selecione a foto ou arquivo do desenho",
            "que você fez sobre o tema 'Solidão Urbana'!"
        };
        g2.setColor(new Color(225, 205, 255, 230));
        for (int i = 0; i < linhas.length; i++) {
            g2.drawString(linhas[i], (W - fmI.stringWidth(linhas[i])) / 2, boxY + 24 + i * 22);
        }

        // Área de preview
        int prevW = 260, prevH = 220;
        int prevX = (W - prevW) / 2, prevY = boxY + boxH + 20;

        for (int i = 4; i >= 1; i--) {
            g2.setColor(new Color(80, 30, 160, 8 * i));
            g2.fillRoundRect(prevX - i * 3, prevY - i * 3, prevW + i * 6, prevH + i * 6, 28, 28);
        }

        g2.setColor(new Color(30, 18, 60, 200));
        g2.fillRoundRect(prevX, prevY, prevW, prevH, 20, 20);

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

        if (previewImg != null) {
            int margin = 10;
            int imgX = prevX + margin, imgY = prevY + margin;
            int imgW = prevW - margin * 2, imgH = prevH - margin * 2;

            Shape oldClip = g2.getClip();
            g2.setClip(new RoundRectangle2D.Float(imgX, imgY, imgW, imgH, 12, 12));
            g2.drawImage(previewImg, imgX, imgY, imgW, imgH, this);
            g2.setClip(oldClip);

            // Badge "Imagem carregada"
            g2.setFont(subFont.deriveFont(13f));
            String badge = "\u2705 Imagem pronta para enviar";
            FontMetrics fmB = g2.getFontMetrics();
            int badgeW = fmB.stringWidth(badge) + 16;
            int badgeH = 24;
            int badgeX = prevX + (prevW - badgeW) / 2;
            int badgeY = prevY + prevH + 8;
            g2.setColor(new Color(40, 160, 80, 200));
            g2.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 12, 12);
            g2.setColor(new Color(220, 255, 220));
            g2.drawString(badge, badgeX + 8, badgeY + 17);
        } else {
            g2.setColor(new Color(80, 50, 140, 60));
            g2.fillRoundRect(prevX + 8, prevY + 8, prevW - 16, prevH - 16, 14, 14);

            int iconSize = 70;
            int iconX = prevX + (prevW - iconSize) / 2;
            int iconY = prevY + (prevH - iconSize) / 2 - 15;

            g2.setColor(new Color(150, 110, 220, (int)(80 + pulse * 50)));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRoundRect(iconX, iconY, iconSize, iconSize, 10, 10);

            int[] mx = {iconX + 10, iconX + iconSize / 2, iconX + iconSize - 10};
            int[] my = {iconY + iconSize - 15, iconY + 30, iconY + iconSize - 15};
            g2.setColor(new Color(140, 100, 220, 100));
            g2.fillPolygon(mx, my, 3);

            g2.setColor(new Color(255, 210, 100, (int)(120 + pulse * 80)));
            g2.fillOval(iconX + iconSize - 30, iconY + 12, 20, 20);

            g2.setFont(subFont.deriveFont(15f));
            FontMetrics fmP = g2.getFontMetrics();
            String ph = "Arraste ou clique abaixo";
            g2.setColor(new Color(180, 150, 230, 160));
            g2.drawString(ph, prevX + (prevW - fmP.stringWidth(ph)) / 2, prevY + prevH - 25);
        }

        // Reposicionar botões
        int btnW = Math.min(420, W - 120), btnH = 48;
        int cx = (W - btnW) / 2;
        int btnStartY = prevY + prevH + 36;

        btnEscolher.setBounds(cx, btnStartY, btnW, btnH);
        btnConfirmar.setBounds(cx, btnStartY + 56, btnW, btnH);
        btnPular.setBounds(cx + 30, btnStartY + 114, btnW - 60, 36);

        // Rodapé
        g2.setFont(subFont.deriveFont(12f));
        g2.setColor(new Color(160, 140, 200, 120));
        String footer = "Formatos aceitos: PNG, JPG, JPEG, BMP";
        FontMetrics fmF = g2.getFontMetrics();
        g2.drawString(footer, (W - fmF.stringWidth(footer)) / 2, H - 15);
    }

    public static boolean deveImportar() {
        return true;
    }
}
