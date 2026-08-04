
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

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

    // Imagem pre-visualizacao
    private BufferedImage previewImg = null;
    private String caminhoSelecionado = null;

    // Fontes
    private Font fonteTitulo, fonteCrayon;

    // Botoes
    private JButton btnEscolher, btnConfirmar, btnPular;

    // Destino da imagem
    public static final String NOME_ARQUIVO_DESENHO = "desenho_jogador.png";

    public ImportadorImagem(JogoAudrey frame, Runnable aoTerminar) {
        this.frame = frame;
        this.aoTerminar = aoTerminar;

        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setLayout(null);
        setBackground(new Color(28, 18, 65));

        carregarFontes();
        criarBotoes();

        timerAnim = new Timer(16, this);
        timerAnim.start();
    }

    // Fontes

    private void carregarFontes() {
        try {
            fonteTitulo = Font.createFont(Font.TRUETYPE_FONT,
                    new File(JogoAudrey.resolvePath("KGSecondChancesSketch.ttf"))).deriveFont(52f);
            fonteCrayon = Font.createFont(Font.TRUETYPE_FONT,
                    new File(JogoAudrey.resolvePath("CrayonHandRegular2016Demo.ttf"))).deriveFont(22f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fonteTitulo);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fonteCrayon);
        } catch (Exception e) {
            fonteTitulo = new Font("Arial", Font.BOLD, 52);
            fonteCrayon = new Font("Arial", Font.PLAIN, 22);
        }
    }

    // Botoes

    private void criarBotoes() {
        btnEscolher = criarBotao("Escolher Imagem do Dispositivo",
                new Color(120, 80, 200), new Color(160, 110, 255));
        btnEscolher.setBounds(300, 500, 400, 52);
        btnEscolher.addActionListener(e -> escolherImagem());
        add(btnEscolher);

        btnConfirmar = criarBotao("Usar esta Imagem",
                new Color(60, 140, 90), new Color(80, 180, 110));
        btnConfirmar.setBounds(300, 565, 400, 52);
        btnConfirmar.setEnabled(false);
        btnConfirmar.addActionListener(e -> confirmarImagem());
        add(btnConfirmar);

        btnPular = criarBotao("Pular (usar imagem padrao)",
                new Color(80, 70, 110), new Color(100, 90, 140));
        btnPular.setBounds(300, 635, 400, 44);
        btnPular.addActionListener(e -> pular());
        add(btnPular);
    }

    private JButton criarBotao(String texto, Color corBase, Color corHover) {
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
                Color c = isEnabled() ? (hover ? corHover : corBase) : new Color(60, 55, 80);
                GradientPaint gp = new GradientPaint(0, 0, c.brighter(), 0, getHeight(), c.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(255, 255, 255, hover ? 100 : 40));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(isEnabled() ? Color.WHITE : new Color(160, 150, 180));
                Font f = fonteCrayon != null ? fonteCrayon.deriveFont(18f) : new Font("Arial", Font.BOLD, 18);
                g2.setFont(f);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
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
        anguloAnim += 0.04f;
        pulsacao    += 0.06f;
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

        // Fundo gradiente
        GradientPaint bg = new GradientPaint(0, 0, new Color(28, 18, 65), W, H, new Color(55, 25, 90));
        g2.setPaint(bg);
        g2.fillRect(0, 0, W, H);

        // Estrelas animadas
        for (int i = 0; i < 70; i++) {
            float cx = (float) ((Math.sin(i * 1.9 + anguloAnim * 0.12) + 1.0) * (W / 2.0));
            float cy = (float) ((Math.cos(i * 2.7 + anguloAnim * 0.08) + 1.0) * (H / 2.0));
            int sz = (i % 3 == 0) ? 3 : 2;
            int alpha = 50 + (i % 5) * 25;
            g2.setColor(new Color(180, 150, 255, Math.min(alpha, 200)));
            g2.fillOval((int) cx, (int) cy, sz, sz);
        }

        // Brilho central pulsante
        float pulse = 0.5f + 0.5f * (float) Math.sin(pulsacao);
        RadialGradientPaint glow = new RadialGradientPaint(
                W / 2f, H / 2f, 320 + pulse * 30,
                new float[]{0f, 1f},
                new Color[]{new Color(140, 80, 220, 70), new Color(0, 0, 0, 0)});
        g2.setPaint(glow);
        g2.fillRect(0, 0, W, H);

        // Titulo
        g2.setFont(fonteTitulo);
        String titulo = "HOBBY QUEST";
        FontMetrics fmT = g2.getFontMetrics();
        int tx = (W - fmT.stringWidth(titulo)) / 2;
        g2.setColor(new Color(30, 15, 70, 160));
        g2.drawString(titulo, tx + 4, 94);
        GradientPaint tituloGrad = new GradientPaint(tx, 60, new Color(220, 180, 255), tx + fmT.stringWidth(titulo), 90, new Color(140, 90, 255));
        g2.setPaint(tituloGrad);
        g2.drawString(titulo, tx, 90);

        // Subtitulo
        Font subFont = (fonteCrayon != null ? fonteCrayon : new Font("Arial", Font.PLAIN, 22)).deriveFont(22f);
        g2.setFont(subFont);
        String sub = "Mostre o seu desenho!";
        FontMetrics fmS = g2.getFontMetrics();
        g2.setColor(new Color(210, 190, 255, 200));
        g2.drawString(sub, (W - fmS.stringWidth(sub)) / 2, 130);

        // Caixa de instrucao
        int boxX = 200, boxY = 155, boxW = 600, boxH = 90;
        g2.setColor(new Color(255, 255, 255, 12));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 20, 20);
        g2.setColor(new Color(200, 170, 255, 60));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 20, 20);

        g2.setFont(subFont.deriveFont(17f));
        FontMetrics fmI = g2.getFontMetrics();
        String[] linhas = {
            "Selecione a foto ou arquivo do desenho",
            "que voce fez sobre o tema 'Solidao Urbana'!"
        };
        g2.setColor(new Color(230, 210, 255));
        for (int i = 0; i < linhas.length; i++) {
            g2.drawString(linhas[i], (W - fmI.stringWidth(linhas[i])) / 2, 185 + i * 28);
        }

        // Area de preview
        int prevX = (W - 220) / 2, prevY = 258, prevW = 220, prevH = 220;
        g2.setColor(new Color(255, 255, 255, 15));
        g2.fillRoundRect(prevX - 4, prevY - 4, prevW + 8, prevH + 8, 24, 24);

        float bAlpha = 80 + pulse * 120;
        g2.setColor(new Color(160, 100, 255, (int) bAlpha));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(prevX - 4, prevY - 4, prevW + 8, prevH + 8, 24, 24);

        if (previewImg != null) {
            g2.drawImage(previewImg, prevX, prevY, prevW, prevH, this);
        } else {
            g2.setColor(new Color(140, 100, 200, 80));
            g2.fillRoundRect(prevX, prevY, prevW, prevH, 20, 20);
            g2.setColor(new Color(180, 140, 255, 150));
            g2.setStroke(new BasicStroke(3f));
            int ic = 60;
            g2.drawRoundRect(prevX + ic / 2, prevY + ic / 2, prevW - ic, prevH - ic, 12, 12);
            int[] mx = {prevX + ic / 2, prevX + prevW / 2 - 15, prevX + prevW / 2 + 5, prevX + prevW - ic / 2};
            int[] my = {prevY + prevH - ic / 2, prevY + prevH / 2 + 15, prevY + prevH / 2 + 35, prevY + prevH - ic / 2};
            g2.setColor(new Color(180, 140, 255, 100));
            g2.fillPolygon(mx, my, 4);
            g2.setColor(new Color(255, 220, 100, 160));
            g2.fillOval(prevX + ic, prevY + ic, 40, 40);
            g2.setFont(subFont.deriveFont(14f));
            FontMetrics fmP = g2.getFontMetrics();
            String ph = "Sua imagem aqui";
            g2.setColor(new Color(200, 170, 255, 180));
            g2.drawString(ph, prevX + (prevW - fmP.stringWidth(ph)) / 2, prevY + prevH + 22);
        }

        // Reposicionar botoes
        int btnW = Math.min(440, W - 100), btnH = 52;
        int cx = (W - btnW) / 2;
        btnEscolher.setBounds(cx, H - 235, btnW, btnH);
        btnConfirmar.setBounds(cx, H - 170, btnW, btnH);
        btnPular.setBounds(cx, H - 100, btnW, 40);
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
