import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import java.io.File;

public class JogoAudrey extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private MenuPrincipal menuPrincipal;
    private MenuEmJogo menuEmJogo;
    private JogoPanel jogoPanel;
    private CutscenePanel cutscenePanel;
    private TelaCarregamento telaCarregamento;
    private MenuSlots menuSlots;
    private boolean jogoIniciado = false;
    private int slotAtual = -1;

    public static final int ACAO_NOVO = 1;
    public static final int ACAO_CONTINUAR = 2;
    public static final int ACAO_APAGAR = 3;
    public static final int ACAO_SALVAR = 4;

    public void setSlotAtual(int slot) {
        this.slotAtual = slot;
    }

    public int getSlotAtual() {
        return this.slotAtual;
    }

    public JogoPanel getJogoPanel() {
        return this.jogoPanel;
    }

    public JogoAudrey() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmarSaida();
            }
        });
        setTitle("Hobby Quest");

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        cutscenePanel = new CutscenePanel(this);
        jogoPanel = new JogoPanel(this);
        menuPrincipal = new MenuPrincipal(this);
        menuEmJogo = new MenuEmJogo(this, jogoPanel);
        telaCarregamento = new TelaCarregamento(this);
        menuSlots = new MenuSlots(this);

        mainPanel.add(telaCarregamento, "carregamento");
        mainPanel.add(cutscenePanel, "cutscene");
        mainPanel.add(menuPrincipal, "menuPrincipal");
        mainPanel.add(jogoPanel, "jogo");
        mainPanel.add(menuEmJogo, "menuEmJogo");
        mainPanel.add(menuSlots, "menuSlots");

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);

        cardLayout.show(mainPanel, "menuPrincipal");
    }

    public void iniciarJogo(int slot) {
        this.slotAtual = slot;
        jogoIniciado = true;
        GerenciadorAudio.tocarSomPlay();
        cardLayout.show(mainPanel, "carregamento");
        telaCarregamento.iniciarCarregamento();
    }

    public void irParaCutscene(int idCutscene) {
        cutscenePanel = new CutscenePanel(this);
        mainPanel.remove(cutscenePanel);
        cutscenePanel = new CutscenePanel(this);
        cutscenePanel.iniciarCutscene(idCutscene);
        mainPanel.add(cutscenePanel, "cutscene");
        mainPanel.revalidate();
        cardLayout.show(mainPanel, "cutscene");
        cutscenePanel.requestFocus();
    }

    public void voltarDeCutscene() {
        if (!jogoIniciado || jogoPanel == null) {
            // É a primeira cutscene, recria o jogo
            recriarJogo();
            cardLayout.show(mainPanel, "jogo");
            jogoPanel.requestFocus();
        } else {
            // É cutscene de meio de jogo
            cardLayout.show(mainPanel, "jogo");
            jogoPanel.requestFocus();
        }
    }

    public void mostrarMenuEmJogo() {
        GerenciadorAudio.tocarSomDialogo();
        cardLayout.show(mainPanel, "menuEmJogo");
    }

    public void continuarJogo() {
        cardLayout.show(mainPanel, "jogo");
        jogoPanel.requestFocus();
    }

    public void continuarJogoSalvo(int slot) {
        if (Database.saveExiste(slot)) {
            this.slotAtual = slot;
            jogoIniciado = true;
            recriarJogo();
            java.util.Properties props = Database.carregarEstado(slot);
            if (props != null) {
                jogoPanel.carregarEstado(props);
            }
            cardLayout.show(mainPanel, "jogo");
            jogoPanel.requestFocus();
        }
    }

    public void voltarAoMenuPrincipal() {
        if (jogoIniciado && jogoPanel != null && slotAtual != -1) {
            jogoPanel.salvarEstado(slotAtual);
        }
        jogoIniciado = false;
        slotAtual = -1;
        mostrarMenuPrincipal();
    }

    public void salvarESair() {
        if (jogoIniciado && jogoPanel != null && slotAtual != -1) {
            jogoPanel.salvarEstado(slotAtual);
        }
        System.exit(0);
    }

    public void confirmarSaida() {
        if (jogoIniciado && jogoPanel != null) {
            int escolha = JOptionPane.showConfirmDialog(
                    this,
                    "Você está prestes a sair.\nDeseja salvar o progresso antes de fechar o jogo?",
                    "Sair do Jogo",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (escolha == JOptionPane.YES_OPTION) {
                if (slotAtual != -1) {
                    jogoPanel.salvarEstado(slotAtual);
                    JOptionPane.showMessageDialog(this, "Jogo salvo no Slot " + slotAtual + " com sucesso!\nAté logo!");
                    System.exit(0);
                } else {
                    JOptionPane.showMessageDialog(this, "Por favor, escolha um slot para salvar antes de sair.");
                    mostrarMenuSlots(ACAO_SALVAR);
                }
            } else if (escolha == JOptionPane.NO_OPTION) {
                System.exit(0); // Sai sem salvar
            }
            // Se cancelar (ou fechar popup), não faz nada e o jogo continua
        } else {
            System.exit(0);
        }
    }

    public void mostrarMenuPrincipal() {
        menuPrincipal.atualizarBotoes();
        cardLayout.show(mainPanel, "menuPrincipal");
    }

    public void mostrarMenuSlots(int acao) {
        menuSlots.preparar(acao);
        cardLayout.show(mainPanel, "menuSlots");
    }

    public MenuPrincipal getMenuPrincipal() {
        return menuPrincipal;
    }

    private void recriarJogo() {
        if (jogoPanel != null) {
            mainPanel.remove(jogoPanel);
        }
        if (menuEmJogo != null) {
            mainPanel.remove(menuEmJogo);
        }

        jogoPanel = new JogoPanel(this);
        menuEmJogo = new MenuEmJogo(this, jogoPanel);

        mainPanel.add(jogoPanel, "jogo");
        mainPanel.add(menuEmJogo, "menuEmJogo");
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JogoAudrey());
    }
}

class TelaCarregamento extends JPanel implements ActionListener {
    private JogoAudrey frame;
    private final int LARGURA = 1000;
    private final int ALTURA = 750;

    private int progresso = 0;
    private Timer timerLoad;
    private Font fontCrayonHand, fontTitulo;
    private float anguloAnimacao = 0;

    public TelaCarregamento(JogoAudrey frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(Color.BLACK);
        carregarFonts();
        timerLoad = new Timer(20, this);
    }

    public void iniciarCarregamento() {
        progresso = 0;
        anguloAnimacao = 0;
        timerLoad.restart();
    }

    private void carregarFonts() {
        try {
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf"))
                    .deriveFont(28f);
            fontTitulo = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf"))
                    .deriveFont(72f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontCrayonHand);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontTitulo);
        } catch (Exception e) {
            fontCrayonHand = new Font("Arial", Font.BOLD, 28);
            fontTitulo = new Font("Arial", Font.BOLD, 72);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        anguloAnimacao += 0.08f;
        progresso += 1;
        repaint();
        if (progresso >= 100) {
            timerLoad.stop();
            frame.irParaCutscene(0); // Cutscene de Intro = 0
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int LARGURA = getWidth();
        int ALTURA = getHeight();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fundo gradiente pastel
        GradientPaint bg = new GradientPaint(
                0, 0, new Color(255, 245, 235), // Creme claro
                LARGURA, ALTURA, new Color(255, 230, 240) // Rosa suave
        );
        g2d.setPaint(bg);
        g2d.fillRect(0, 0, LARGURA, ALTURA);

        // Estrelas animadas de fundo
        for (int i = 0; i < 60; i++) {
            float cx2 = (float) ((Math.sin(i * 1.7 + anguloAnimacao * 0.15) + 1.0) * 500);
            float cy2 = (float) ((Math.cos(i * 2.3 + anguloAnimacao * 0.1) + 1.0) * 375);
            int sz = (i % 3 == 0) ? 3 : 2;
            int alpha = 60 + (i % 4) * 30;
            g2d.setColor(new Color(180, 230, 180, Math.min(alpha, 180))); // Verde pastel
            g2d.fillOval((int) cx2, (int) cy2, sz, sz);
        }

        // Brilho central suave
        RadialGradientPaint glow = new RadialGradientPaint(
                LARGURA / 2f, ALTURA / 2f, 350,
                new float[] { 0f, 1f },
                new Color[] { new Color(255, 200, 200, 80), new Color(255, 255, 255, 0) } // Rosa brilhante
        );
        g2d.setPaint(glow);
        g2d.fillRect(0, 0, LARGURA, ALTURA);

        // Título
        g2d.setFont(fontTitulo);
        String titulo = "HOBBY QUEST";
        FontMetrics fmT = g2d.getFontMetrics();
        int tx = (LARGURA - fmT.stringWidth(titulo)) / 2;

        // Sombra do título
        g2d.setColor(new Color(200, 150, 180, 120)); // Rosa escuro pra sombra
        g2d.drawString(titulo, tx + 5, 255);

        // Título com gradiente
        GradientPaint titleGrad = new GradientPaint(
                tx, 180, new Color(255, 150, 180), // Rosa forte
                tx + fmT.stringWidth(titulo), 250, new Color(180, 230, 180) // Verde claro
        );
        g2d.setPaint(titleGrad);
        g2d.drawString(titulo, tx, 250);

        // Subtítulo
        g2d.setFont(fontCrayonHand.deriveFont(22f));
        g2d.setColor(new Color(180, 140, 160)); // Rosa mais escuro e calmo
        String sub = "Preparando a aventura...";
        FontMetrics fmS = g2d.getFontMetrics();
        g2d.drawString(sub, (LARGURA - fmS.stringWidth(sub)) / 2, 320);

        // --- Barra de progresso ---
        int barW = Math.min(600, LARGURA - 100);
        int barX = (LARGURA - barW) / 2;
        int barY = ALTURA / 2 + 55;
        int barH = 28;

        // Fundo da barra
        g2d.setColor(new Color(255, 255, 255, 150)); // Branco transparente
        g2d.fillRoundRect(barX, barY, barW, barH, 14, 14);
        g2d.setColor(new Color(180, 230, 180)); // Borda verde pastel
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(barX, barY, barW, barH, 14, 14);

        // Preenchimento da barra
        int barFill = (int) (barW * progresso / 100.0);
        if (barFill > 0) {
            GradientPaint barGrad = new GradientPaint(
                    barX, barY, new Color(255, 180, 200), // Rosa suave
                    barX + barFill, barY, new Color(255, 220, 230) // Rosa mais claro
            );
            g2d.setPaint(barGrad);
            g2d.fillRoundRect(barX, barY, barFill, barH, 14, 14);
            // Reflexo brilhante na barra
            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.fillRoundRect(barX + 2, barY + 2, barFill - 4, barH / 2 - 2, 10, 10);
        }

        // Porcentagem
        g2d.setFont(fontCrayonHand.deriveFont(20f));
        g2d.setColor(new Color(120, 80, 100)); // Rosa escuro (pra leitura)
        String pct = progresso + "%";
        FontMetrics fmP = g2d.getFontMetrics();
        g2d.drawString(pct, (LARGURA - fmP.stringWidth(pct)) / 2, barY + barH + 38);

        // Spinner animado
        int spinX = LARGURA / 2;
        int spinY = barY + barH + 112;
        int r = 18;
        g2d.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 8; i++) {
            float angle = (float) (i * Math.PI / 4 + anguloAnimacao);
            float alpha2 = (i + 1) / 8.0f;
            g2d.setColor(new Color(180, 230, 180, (int) (alpha2 * 255))); // Verde pastel
            int x1 = spinX + (int) (Math.cos(angle) * (r - 6));
            int y1 = spinY + (int) (Math.sin(angle) * (r - 6));
            int x2 = spinX + (int) (Math.cos(angle) * r);
            int y2 = spinY + (int) (Math.sin(angle) * r);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Texto "Carregando..."
        g2d.setFont(fontCrayonHand.deriveFont(16f));
        g2d.setColor(new Color(150, 120, 140));
        String loading = "Carregando...";
        FontMetrics fmL = g2d.getFontMetrics();
        g2d.drawString(loading, (LARGURA - fmL.stringWidth(loading)) / 2, spinY + r + 30);

        // Créditos rodapé
        g2d.setFont(fontCrayonHand.deriveFont(13f));
        g2d.setColor(new Color(120, 100, 160));
        String credito = "Desenvolvido com Java Swing";
        FontMetrics fmC = g2d.getFontMetrics();
        g2d.drawString(credito, (LARGURA - fmC.stringWidth(credito)) / 2, ALTURA - 25);
    }
}

class MenuPrincipal extends JPanel {
    private JogoAudrey frame;
    private JButton btnPlay, btnContinuar, btnSobre, btnSair;
    private Font fontCrayonHand, fontTitulo;
    private Color corPrincipal;

    public MenuPrincipal(JogoAudrey frame) {
        this.frame = frame;
        corPrincipal = new Color(255, 230, 235);
        setBackground(new Color(255, 245, 235));
        setLayout(null);

        carregarFonts();
        criarComponentes();
    }

    private void carregarFonts() {
        try {
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf"))
                    .deriveFont(24f);
            fontTitulo = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf"))
                    .deriveFont(72f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontCrayonHand);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontTitulo);
        } catch (Exception e) {
            fontCrayonHand = new Font("Arial", Font.BOLD, 24);
            fontTitulo = new Font("Arial", Font.BOLD, 72);
        }
    }

    private void criarComponentes() {
        btnPlay = criarBotao("NOVO JOGO");
        btnPlay.addActionListener(e -> frame.mostrarMenuSlots(JogoAudrey.ACAO_NOVO));
        add(btnPlay);

        btnContinuar = criarBotao("CONTINUAR");
        btnContinuar.addActionListener(e -> frame.mostrarMenuSlots(JogoAudrey.ACAO_CONTINUAR));
        add(btnContinuar);

        btnSobre = criarBotao("SOBRE O JOGO");
        btnSobre.addActionListener(e -> mostrarSobre());
        add(btnSobre);

        btnSair = criarBotao("SAIR");
        btnSair.addActionListener(e -> frame.confirmarSaida());
        add(btnSair);

        atualizarBotoes();
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int w = getWidth();
        int h = getHeight();
        int bw = Math.max(240, w / 4);
        int bh = Math.max(55, h / 11);
        int bx = (w - bw) / 2;
        int startY = (int) (h * 0.35);
        int gap = (int) (h * 0.11);
        JButton[] btns = { btnPlay, btnContinuar, btnSobre, btnSair };
        for (int i = 0; i < btns.length; i++) {
            if (btns[i] != null)
                btns[i].setBounds(bx, startY + i * gap, bw, bh);
        }
    }

    public void atualizarBotoes() {
        if (btnContinuar != null) {
            boolean existe = Database.saveExiste(1) || Database.saveExiste(2) || Database.saveExiste(3);
            btnContinuar.setEnabled(existe);
            btnContinuar.setBackground(existe ? corPrincipal : new Color(240, 220, 225));
            btnContinuar.setForeground(existe ? new Color(120, 80, 100) : Color.GRAY);
        }
    }

    private JButton criarBotao(String texto) {
        return new BotaoEstilizado(texto, fontCrayonHand);
    }

    private void mostrarSobre() {
        String mensagem = "Hobby Quest\n\n" +
                "Um jogo de aventura onde você ajuda Audrey\n" +
                "a resolver mistérios e coletar itens especiais.\n\n" +
                "CONTROLES:\n" +
                "A - Mover para esquerda\n" +
                "D - Mover para direita\n" +
                "E - Interagir com objetos e pegar itens\n" +
                "F - Falar com personagens\n" +
                "Q - Fechar diálogos e armário\n" +
                "B - Abrir/Fechar inventário\n" +
                "M - Mostrar/Esconder objetivos\n" +
                "ESC - Abrir menu em jogo\n\n" +
                "Desenvolvido com Java Swing";

        JOptionPane.showMessageDialog(
                this,
                mensagem,
                "Sobre o Jogo",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int W = getWidth(), H = getHeight();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(0, 0, new Color(255, 245, 235), 0, H, new Color(255, 230, 240));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, W, H);

        desenharRabiscosMenu(g2d, W, H);

        int titleY = (int) (H * 0.20);
        g2d.setFont(fontTitulo);
        String titulo = "Hobby Quest";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (W - fm.stringWidth(titulo)) / 2;

        g2d.setColor(new Color(200, 150, 180, 120));
        g2d.drawString(titulo, x + 4, titleY + 4);

        GradientPaint titleGrad = new GradientPaint(x, titleY - 50, new Color(255, 150, 180), x, titleY,
                new Color(180, 230, 180));
        g2d.setPaint(titleGrad);
        g2d.drawString(titulo, x, titleY);
    }

    private void desenharRabiscosMenu(Graphics2D g2d, int w, int h) {
        Stroke s = new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(s);

        // Flor (topo esquerdo)
        mFlor(g2d, new Color(255, 175, 200, 170), (int) (w * 0.09), (int) (h * 0.13), 22);
        // Estrela 4pts (topo direito)
        mEstrela4(g2d, new Color(255, 215, 100, 170), (int) (w * 0.88), (int) (h * 0.10), 16);
        // Nuvem (esquerda meio)
        mNuvem(g2d, new Color(170, 205, 255, 150), (int) (w * 0.05), (int) (h * 0.42));
        // Coração (direita meio)
        mCoracao(g2d, new Color(255, 155, 190, 160), (int) (w * 0.87), (int) (h * 0.50), 22);
        // Laço (baixo esquerdo)
        mLaco(g2d, new Color(255, 185, 210, 160), (int) (w * 0.08), (int) (h * 0.77));
        // Estrela 5pts (baixo direito)
        mEstrela5(g2d, new Color(255, 200, 110, 165), (int) (w * 0.89), (int) (h * 0.80), 20);
        // Espiral (baixo centro-direita)
        mEspiral(g2d, new Color(180, 195, 255, 155), (int) (w * 0.80), (int) (h * 0.20));

        // Bolinhas decorativas
        g2d.setColor(new Color(255, 175, 210, 120));
        for (int i = 0; i < 4; i++)
            g2d.fillOval((int) (w * 0.20) + i * 20, (int) (h * 0.88), 9, 9);
        g2d.setColor(new Color(180, 230, 180, 110));
        for (int i = 0; i < 3; i++)
            g2d.fillOval((int) (w * 0.68) + i * 22, (int) (h * 0.84), 10, 10);
    }

    private void mFlor(Graphics2D g2d, Color c, int cx, int cy, int r) {
        g2d.setColor(c);
        for (int i = 0; i < 6; i++) {
            double a = Math.PI / 3 * i;
            int px = cx + (int) (Math.cos(a) * r), py = cy + (int) (Math.sin(a) * r);
            g2d.drawOval(px - r / 2, py - r / 2, r, r);
        }
        g2d.fillOval(cx - r / 3, cy - r / 3, 2 * r / 3, 2 * r / 3);
    }

    private void mEstrela4(Graphics2D g2d, Color c, int cx, int cy, int r) {
        g2d.setColor(c);
        int r2 = r / 3;
        g2d.drawLine(cx - r, cy, cx + r, cy);
        g2d.drawLine(cx, cy - r, cx, cy + r);
        g2d.drawLine(cx - r2, cy - r2, cx + r2, cy + r2);
        g2d.drawLine(cx - r2, cy + r2, cx + r2, cy - r2);
    }

    private void mNuvem(Graphics2D g2d, Color c, int cx, int cy) {
        g2d.setColor(c);
        g2d.drawOval(cx, cy, 32, 22);
        g2d.drawOval(cx + 12, cy - 12, 28, 28);
        g2d.drawOval(cx + 28, cy, 32, 22);
    }

    private void mCoracao(Graphics2D g2d, Color c, int cx, int cy, int sz) {
        g2d.setColor(c);
        GeneralPath p = new GeneralPath();
        p.moveTo(cx, cy + sz / 2);
        p.curveTo(cx, cy - sz / 2, cx - sz, cy - sz / 2, cx - sz, cy);
        p.curveTo(cx - sz, cy + sz * 0.7, cx, cy + sz * 1.3, cx, cy + sz / 2);
        p.curveTo(cx, cy + sz * 1.3, cx + sz, cy + sz * 0.7, cx + sz, cy);
        p.curveTo(cx + sz, cy - sz / 2, cx, cy - sz / 2, cx, cy + sz / 2);
        g2d.draw(p);
    }

    private void mLaco(Graphics2D g2d, Color c, int cx, int cy) {
        g2d.setColor(c);
        g2d.drawOval(cx - 28, cy - 13, 28, 24);
        g2d.drawOval(cx, cy - 13, 28, 24);
        g2d.fillOval(cx - 5, cy - 5, 10, 10);
        g2d.drawLine(cx - 14, cy + 11, cx - 22, cy + 28);
        g2d.drawLine(cx + 14, cy + 11, cx + 22, cy + 28);
    }

    private void mEstrela5(Graphics2D g2d, Color c, int cx, int cy, int r) {
        g2d.setColor(c);
        GeneralPath star = new GeneralPath();
        int ri = r / 2;
        for (int i = 0; i < 10; i++) {
            double a = Math.PI / 5 * i - Math.PI / 2;
            int rr = (i % 2 == 0) ? r : ri;
            int px = cx + (int) (Math.cos(a) * rr), py = cy + (int) (Math.sin(a) * rr);
            if (i == 0)
                star.moveTo(px, py);
            else
                star.lineTo(px, py);
        }
        star.closePath();
        g2d.draw(star);
    }

    private void mEspiral(Graphics2D g2d, Color c, int cx, int cy) {
        g2d.setColor(c);
        GeneralPath sp = new GeneralPath();
        double rad = 18, ang = 0;
        sp.moveTo(cx + rad, cy);
        for (int i = 1; i <= 50; i++) {
            ang = i * Math.PI / 12;
            rad = 18 - i * 0.33;
            if (rad < 0)
                break;
            sp.lineTo(cx + rad * Math.cos(ang), cy + rad * Math.sin(ang));
        }
        g2d.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(sp);
    }
}

class MenuEmJogo extends JPanel {
    private JogoAudrey frame;
    private JogoPanel jogoPanel;
    private JButton btnContinuar, btnSalvar, btnSobre, btnMenuPrincipal, btnSair;
    private Font fontCrayonHand, fontTitulo;
    private Color corPrincipal;

    public MenuEmJogo(JogoAudrey frame, JogoPanel jogoPanel) {
        this.frame = frame;
        this.jogoPanel = jogoPanel;
        corPrincipal = new Color(255, 230, 235); // Rosa pastel claro
        setBackground(new Color(255, 245, 235)); // Creme pastel
        setLayout(null);

        carregarFonts();
        criarComponentes();
    }

    private void carregarFonts() {
        try {
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf"))
                    .deriveFont(24f);
            fontTitulo = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf"))
                    .deriveFont(60f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontCrayonHand);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontTitulo);
        } catch (Exception e) {
            fontCrayonHand = new Font("Arial", Font.BOLD, 24);
            fontTitulo = new Font("Arial", Font.BOLD, 60);
        }
    }

    private void criarComponentes() {
        btnContinuar = criarBotao("VOLTAR AO JOGO");
        btnContinuar.addActionListener(e -> frame.continuarJogo());
        add(btnContinuar);

        btnSalvar = criarBotao("SALVAR JOGO");
        btnSalvar.addActionListener(e -> {
            int slot = frame.getSlotAtual();
            if (slot != -1) {
                frame.getJogoPanel().salvarEstado(slot);
                JOptionPane.showMessageDialog(this, "Jogo salvo com sucesso no Slot " + slot + "!");
            } else {
                frame.mostrarMenuSlots(JogoAudrey.ACAO_SALVAR);
            }
        });
        add(btnSalvar);

        btnSobre = criarBotao("SOBRE O JOGO");
        btnSobre.addActionListener(e -> mostrarSobre());
        add(btnSobre);

        btnMenuPrincipal = criarBotao("MENU PRINCIPAL");
        btnMenuPrincipal.addActionListener(e -> frame.voltarAoMenuPrincipal());
        add(btnMenuPrincipal);

        btnSair = criarBotao("SAIR");
        btnSair.addActionListener(e -> frame.confirmarSaida());
        add(btnSair);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int w = getWidth();
        int h = getHeight();
        int bw = Math.max(240, w / 4);
        int bh = Math.max(55, h / 11);
        int bx = (w - bw) / 2;
        int startY = (int) (h * 0.35);
        int gap = (int) (h * 0.11);
        JButton[] btns = { btnContinuar, btnSalvar, btnSobre, btnMenuPrincipal, btnSair };
        for (int i = 0; i < btns.length; i++) {
            if (btns[i] != null)
                btns[i].setBounds(bx, startY + i * gap, bw, bh);
        }
    }

    private JButton criarBotao(String texto) {
        JButton botao = new JButton(texto);
        botao.setFont(fontCrayonHand);
        botao.setBackground(corPrincipal);
        botao.setForeground(new Color(120, 80, 100)); // Rosa escuro
        botao.setBorder(BorderFactory.createLineBorder(new Color(180, 230, 180), 3, true)); // Verde pastel
        botao.setFocusPainted(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));

        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (botao.isEnabled()) {
                    botao.setBackground(new Color(255, 210, 220)); // Hover rosa
                    botao.setFont(fontCrayonHand.deriveFont(Font.BOLD));
                    botao.setBorder(BorderFactory.createLineBorder(new Color(150, 210, 150), 3, true)); // Hover verde
                                                                                                        // escuro
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (botao.isEnabled()) {
                    botao.setBackground(corPrincipal);
                    botao.setFont(fontCrayonHand);
                    botao.setBorder(BorderFactory.createLineBorder(new Color(180, 230, 180), 3, true));
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (botao.isEnabled()) {
                    botao.setBackground(new Color(255, 190, 205)); // Pressed rosa
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (botao.isEnabled()) {
                    botao.setBackground(new Color(255, 210, 220));
                }
            }
        });

        return botao;
    }

    private void mostrarSobre() {
        String mensagem = "Hobby Quest\n\n" +
                "Um jogo de aventura onde você ajuda Audrey\n" +
                "a resolver mistérios e coletar itens especiais.\n\n" +
                "CONTROLES:\n" +
                "A - Mover para esquerda\n" +
                "D - Mover para direita\n" +
                "E - Interagir com objetos e pegar itens\n" +
                "F - Falar com personagens\n" +
                "Q - Fechar diálogos e armário\n" +
                "B - Abrir/Fechar inventário\n" +
                "M - Mostrar/Esconder objetivos\n" +
                "ESC - Abrir menu em jogo\n\n" +
                "Desenvolvido com Java Swing";

        JOptionPane.showMessageDialog(
                this,
                mensagem,
                "Sobre o Jogo",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(255, 245, 235), // Creme pastel
                0, getHeight(), new Color(255, 230, 240) // Rosa pastel
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setFont(fontTitulo);
        String titulo = "PAUSADO";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(titulo)) / 2;

        g2d.setColor(new Color(200, 150, 180, 120)); // Sombra rosa pastel
        g2d.drawString(titulo, x + 4, 154);

        GradientPaint titleGrad = new GradientPaint(
                x, 100, new Color(255, 150, 180), // Rosa forte
                x, 160, new Color(180, 230, 180) // Verde claro
        );
        g2d.setPaint(titleGrad);
        g2d.drawString(titulo, x, 150);
    }
}

class JogoPanel extends JPanel implements ActionListener, KeyListener, MouseListener {
    private JogoAudrey frame;
    private final int LARGURA = 1000;
    private final int ALTURA = 750;

    private Image fundoCenario1, fundoCenario2, fundoCenario3, imgArmarioAberto, imgNicolas;
    private Image imgChave, imgLivro;
    private Image imgAlunoCorredor1, imgAlunoCorredor2;
    private Image[] framesAndar = new Image[2];
    private Image imgParada;

    private final int AUDREY_LARGURA = 240;
    private final int AUDREY_ALTURA = 430;
    private int audreyX = 100, audreyY = 730, velX = 0;
    private int frameAtual = 0, contadorAnimacao = 0;

    private final int NICOLAS_LARGURA = 350;
    private final int NICOLAS_ALTURA = 520;

    private boolean olhandoDireita = true, estaMovendo = false;
    private boolean temChave = false, armarioEstaAberto = false;
    private boolean estaEmDialogoNicolas = false;
    private boolean nicolasJaFoiEncontrado = false;
    private String[] dialogoNicolasIntro = {
            "Oi, eu sou o Nicolas! Prazer em conhecer você, Audrey.",
            "Sou o responsável por ajudar os alunos com as missões aqui.",
            "Se precisar de alguma coisa, é só chamar!",
            "Vamos começar sua aventura juntos!"
    };
    private boolean primeiroEncontroNicolas = false;
    private boolean mostrando_chave = false;
    private boolean inventarioAberto = false;
    private boolean temLivro = false;
    private boolean livroJoiFoiPego = false;
    private boolean mostrarObjetivos = false;
    private int contadorEfeitoChave = 0;

    // --- NOVAS VARIÁVEIS DE HOBBIES E DIÁRIO ---
    private int nivelLeitura = 1;
    private int nivelArte = 1;
    private int nivelFitness = 1;

    private boolean missaoLeituraAtiva = false;
    private boolean missaoArteAtiva = false;
    private boolean missaoFitnessAtiva = false;

    private boolean missaoLeituraConcluida = false;
    private boolean missaoArteConcluida = false;
    private boolean missaoFitnessConcluida = false;

    private boolean cutsceneSalaVista = false;
    private boolean cutsceneFinalVista = false;
    private boolean temCadernoEsboco = false;
    private boolean temCronograma = false;

    private boolean explorouCorredor = false;
    private boolean diarioAberto = false;
    // -------------------------------------------

    private int indiceMapa = 0, faseDialogoNicolas = 0;
    private int ultimaPosAoEntraSala = 0;

    private int posArmarioX = 650;
    private int posNicolasXSalaAula = 800;
    private int posPortaX = 500;

    private int puertaSaidaX = 20;
    private int puertaSaidaLargura = 80;

    private String textoDialogo = "";
    private String nomePersonagem = "";

    private int faseDialogoLeitura = 0;
    private int faseDialogoArte = 0;
    private int faseDialogoFitness = 0;

    private Font fontCrayonHand;

    public int indiceMapa_public = 0;
    public int audreyX_public = 100;

    public void salvarEstado(int slot) {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("indiceMapa", String.valueOf(indiceMapa));
        props.setProperty("audreyX", String.valueOf(audreyX));
        props.setProperty("temChave", String.valueOf(temChave));
        props.setProperty("armarioEstaAberto", String.valueOf(armarioEstaAberto));
        props.setProperty("nicolasJaFoiEncontrado", String.valueOf(nicolasJaFoiEncontrado));
        props.setProperty("livroJoiFoiPego", String.valueOf(livroJoiFoiPego));
        props.setProperty("temLivro", String.valueOf(temLivro));
        props.setProperty("faseDialogoNicolas", String.valueOf(faseDialogoNicolas));

        props.setProperty("nivelLeitura", String.valueOf(nivelLeitura));
        props.setProperty("nivelArte", String.valueOf(nivelArte));
        props.setProperty("nivelFitness", String.valueOf(nivelFitness));
        props.setProperty("missaoLeituraAtiva", String.valueOf(missaoLeituraAtiva));
        props.setProperty("missaoArteAtiva", String.valueOf(missaoArteAtiva));
        props.setProperty("missaoFitnessAtiva", String.valueOf(missaoFitnessAtiva));
        props.setProperty("missaoLeituraConcluida", String.valueOf(missaoLeituraConcluida));
        props.setProperty("missaoArteConcluida", String.valueOf(missaoArteConcluida));
        props.setProperty("missaoFitnessConcluida", String.valueOf(missaoFitnessConcluida));
        props.setProperty("cutsceneSalaVista", String.valueOf(cutsceneSalaVista));
        props.setProperty("cutsceneFinalVista", String.valueOf(cutsceneFinalVista));
        props.setProperty("temCadernoEsboco", String.valueOf(temCadernoEsboco));
        props.setProperty("temCronograma", String.valueOf(temCronograma));
        props.setProperty("explorouCorredor", String.valueOf(explorouCorredor));

        Database.salvarEstado(slot, props);
    }

    public void carregarEstado(java.util.Properties props) {
        indiceMapa = Integer.parseInt(props.getProperty("indiceMapa", "0"));
        audreyX = Integer.parseInt(props.getProperty("audreyX", "100"));
        temChave = Boolean.parseBoolean(props.getProperty("temChave", "false"));
        armarioEstaAberto = Boolean.parseBoolean(props.getProperty("armarioEstaAberto", "false"));
        nicolasJaFoiEncontrado = Boolean.parseBoolean(props.getProperty("nicolasJaFoiEncontrado", "false"));
        livroJoiFoiPego = Boolean.parseBoolean(props.getProperty("livroJoiFoiPego", "false"));
        temLivro = Boolean.parseBoolean(props.getProperty("temLivro", "false"));
        faseDialogoNicolas = Integer.parseInt(props.getProperty("faseDialogoNicolas", "0"));

        nivelLeitura = Integer.parseInt(props.getProperty("nivelLeitura", "1"));
        nivelArte = Integer.parseInt(props.getProperty("nivelArte", "1"));
        nivelFitness = Integer.parseInt(props.getProperty("nivelFitness", "1"));
        missaoLeituraAtiva = Boolean.parseBoolean(props.getProperty("missaoLeituraAtiva", "false"));
        missaoArteAtiva = Boolean.parseBoolean(props.getProperty("missaoArteAtiva", "false"));
        missaoFitnessAtiva = Boolean.parseBoolean(props.getProperty("missaoFitnessAtiva", "false"));
        missaoLeituraConcluida = Boolean.parseBoolean(props.getProperty("missaoLeituraConcluida", "false"));
        missaoArteConcluida = Boolean.parseBoolean(props.getProperty("missaoArteConcluida", "false"));
        missaoFitnessConcluida = Boolean.parseBoolean(props.getProperty("missaoFitnessConcluida", "false"));
        cutsceneSalaVista = Boolean.parseBoolean(props.getProperty("cutsceneSalaVista", "false"));
        cutsceneFinalVista = Boolean.parseBoolean(props.getProperty("cutsceneFinalVista", "false"));
        temCadernoEsboco = Boolean.parseBoolean(props.getProperty("temCadernoEsboco", "false"));
        temCronograma = Boolean.parseBoolean(props.getProperty("temCronograma", "false"));
        explorouCorredor = Boolean.parseBoolean(props.getProperty("explorouCorredor", "false"));

        textoDialogo = "";
        nomePersonagem = "";
        estaEmDialogoNicolas = false;
        inventarioAberto = false;
        mostrarObjetivos = false;
        diarioAberto = false;
        repaint();
    }

    public JogoPanel(JogoAudrey frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        setDoubleBuffered(true);

        carregarFonts();
        carregarAssets();
        new Timer(16, this).start();
    }

    private void carregarFonts() {
        try {
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf"))
                    .deriveFont(14f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontCrayonHand);
        } catch (Exception e) {
            fontCrayonHand = new Font("Arial", Font.PLAIN, 14);
        }
    }

    private void carregarAssets() {
        fundoCenario1 = new ImageIcon("corredor1.png").getImage();
        fundoCenario2 = new ImageIcon("corredor2.png").getImage();
        fundoCenario3 = new ImageIcon("saladeaulanico.png").getImage();
        imgArmarioAberto = new ImageIcon("imagemarmario.png").getImage();
        imgNicolas = new ImageIcon("nico__1_-removebg-preview.png").getImage();
        imgChave = new ImageIcon("chave.png").getImage();
        imgLivro = new ImageIcon("livro.png").getImage();

        imgAlunoCorredor1 = carregarImagem("aluno_corredor1.png");
        imgAlunoCorredor2 = carregarImagem("aluno_corredor2.png");

        framesAndar[0] = redimensionarImagem(new ImageIcon("1-removebg-preview3.png").getImage(), AUDREY_LARGURA,
                AUDREY_ALTURA);
        framesAndar[1] = redimensionarImagem(new ImageIcon("2-removebg-preview65.png").getImage(), AUDREY_LARGURA,
                AUDREY_ALTURA);

        imgParada = redimensionarImagem(new ImageIcon("Parada_fundo_verde-removebg-preview24.png").getImage(),
                AUDREY_LARGURA, AUDREY_ALTURA);
    }

    private Image carregarImagem(String caminho) {
        try {
            return ImageIO.read(new File(caminho));
        } catch (Exception e) {
            System.err.println("[ERRO] Não foi possível carregar: " + caminho);
            return null;
        }
    }

    private Image redimensionarImagem(Image img, int width, int height) {
        return img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int w = getWidth();
        int h = getHeight();

        // Calcular a escala preservando a proporção de 1000x750
        double scaleX = (double) w / LARGURA;
        double scaleY = (double) h / ALTURA;
        double scale = Math.min(scaleX, scaleY);

        int xOffset = (int) ((w - (LARGURA * scale)) / 2);
        int yOffset = (int) ((h - (ALTURA * scale)) / 2);

        // Fundo preto para as bordas (se sobrar espaço)
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, w, h);

        g2d.translate(xOffset, yOffset);
        g2d.scale(scale, scale);
        g2d.setClip(0, 0, LARGURA, ALTURA);

        if (!armarioEstaAberto) {
            if (indiceMapa == 0) {
                g2d.drawImage(fundoCenario1, 0, 0, LARGURA, ALTURA, this);
            } else if (indiceMapa == 1) {
                g2d.drawImage(fundoCenario2, 0, 0, LARGURA, ALTURA, this);
            } else if (indiceMapa == 2) {
                g2d.drawImage(fundoCenario3, 0, 0, LARGURA, ALTURA, this);
            }

            if (indiceMapa == 2) {
                if (imgNicolas != null) {
                    g2d.drawImage(imgNicolas, posNicolasXSalaAula - NICOLAS_LARGURA / 2, audreyY - NICOLAS_ALTURA,
                            NICOLAS_LARGURA, NICOLAS_ALTURA, this);
                }
            }

            if (indiceMapa == 1) {
                // Estudantes no final do corredor - mesma largura do Nicolas, mas mais baixos
                int stX = 850;
                int larguraPersonagem = NICOLAS_LARGURA + 100; // 450px de largura
                int alturaPersonagem = NICOLAS_ALTURA - 20; // 500px de altura

                if (imgAlunoCorredor1 != null) {
                    g2d.drawImage(imgAlunoCorredor1, stX - 120, audreyY - alturaPersonagem, larguraPersonagem,
                            alturaPersonagem, this);
                }
                if (imgAlunoCorredor2 != null) {
                    // Invertendo a segunda imagem para eles parecerem conversar
                    g2d.drawImage(imgAlunoCorredor2, stX + 80, audreyY - alturaPersonagem, -larguraPersonagem,
                            alturaPersonagem, this);
                }
            }

            Image img;

            if (!estaMovendo && imgParada != null) {
                img = imgParada;
            } else {
                img = framesAndar[frameAtual];
            }

            if (olhandoDireita) {
                g2d.drawImage(img, audreyX, audreyY - AUDREY_ALTURA, AUDREY_LARGURA, AUDREY_ALTURA, this);
            } else {
                g2d.drawImage(img, audreyX + AUDREY_LARGURA, audreyY - AUDREY_ALTURA, -AUDREY_LARGURA, AUDREY_ALTURA,
                        this);
            }

            // --- SHADER: Tom Pastel Levíssimo ---
            // Véu rosa-creme uniforme, quase invisível, só aquece as cores
            g2d.setColor(new Color(255, 235, 240, 18));
            g2d.fillRect(0, 0, LARGURA, ALTURA);
            // ----------------------------------------

            desenharInterface(g2d);

            if (inventarioAberto) {
                desenharTelaInventario(g2d);
            } else {
                if (mostrarObjetivos) {
                    desenharObjetivos(g2d);
                }
            }

            if (mostrando_chave) {
                desenharEfeitoChave(g2d);
            }
        } else {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, LARGURA, ALTURA);
            if (imgArmarioAberto != null) {
                int imgW = imgArmarioAberto.getWidth(this);
                int imgH = imgArmarioAberto.getHeight(this);
                double escala = Math.min((double) LARGURA / imgW, (double) (ALTURA - 150) / imgH);
                int lw = (int) (imgW * escala);
                int lh = (int) (imgH * escala);
                g2d.drawImage(imgArmarioAberto, (LARGURA - lw) / 2, (ALTURA - 150 - lh) / 2, lw, lh, this);
            }

            if (!livroJoiFoiPego) {
                desenharLivroNoArmario(g2d);
            }

            desenharInterface(g2d);

            if (inventarioAberto) {
                desenharTelaInventario(g2d);
            } else {
                if (mostrarObjetivos) {
                    desenharObjetivos(g2d);
                }
            }
        }

        if (diarioAberto) {
            desenharDiario(g2d);
        }
    }

    private void desenharLivroNoArmario(Graphics2D g2d) {
        if (imgLivro != null) {
            Image livroRedimensionado = redimensionarImagem(imgLivro, 80, 100);
            int livroX = LARGURA / 2 - 40;
            int livroY = ALTURA / 2 - 50;
            g2d.drawImage(livroRedimensionado, livroX, livroY, this);

            g2d.setFont(fontCrayonHand.deriveFont(Font.BOLD, 18));
            g2d.setColor(new Color(255, 215, 0));
            g2d.drawString("Livro encontrado!", LARGURA / 2 - 90, ALTURA / 2 + 80);

            g2d.setFont(fontCrayonHand.deriveFont(Font.PLAIN, 14));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Pressione [E] para pegar", LARGURA / 2 - 110, ALTURA / 2 + 110);
        }
    }

    private void desenharTelaInventario(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, LARGURA, ALTURA);

        int caixaX = 150;
        int caixaY = 50;
        int caixaLargura = LARGURA - 300;
        int caixaAltura = ALTURA - 100;

        g2d.setColor(new Color(50, 50, 80, 250));
        g2d.fillRoundRect(caixaX, caixaY, caixaLargura, caixaAltura, 20, 20);

        g2d.setColor(new Color(200, 200, 0));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRoundRect(caixaX, caixaY, caixaLargura, caixaAltura, 20, 20);

        g2d.setFont(fontCrayonHand.deriveFont(Font.BOLD, 32));
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString("INVENTARIO", caixaX + 150, caixaY + 50);

        g2d.setColor(new Color(200, 200, 0));
        g2d.drawLine(caixaX + 20, caixaY + 70, caixaX + caixaLargura - 20, caixaY + 70);

        int itemY = caixaY + 120;
        int itemCount = 0;

        if (temChave) {
            if (imgChave != null) {
                Image chaveGrande = redimensionarImagem(imgChave, 60, 60);
                g2d.drawImage(chaveGrande, caixaX + 50, itemY - 30, this);
            }

            g2d.setFont(fontCrayonHand.deriveFont(Font.BOLD, 20));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Chave do Armário", caixaX + 130, itemY + 10);

            g2d.setFont(fontCrayonHand.deriveFont(Font.PLAIN, 14));
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawString("Obtida de: Garoto com livro (sala de aula)", caixaX + 130, itemY + 35);

            itemY += 80;
            itemCount++;
        }

        if (temLivro) {
            if (imgLivro != null) {
                Image livroGrande = redimensionarImagem(imgLivro, 60, 60);
                g2d.drawImage(livroGrande, caixaX + 50, itemY - 30, this);
            }

            g2d.setFont(fontCrayonHand.deriveFont(Font.BOLD, 20));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Livro Misterioso", caixaX + 130, itemY + 10);

            g2d.setFont(fontCrayonHand.deriveFont(Font.PLAIN, 14));
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawString("Encontrado no armário", caixaX + 130, itemY + 35);

            itemCount++;
            itemY += 80;
        }

        if (temCadernoEsboco) {
            g2d.setFont(fontCrayonHand.deriveFont(Font.BOLD, 20));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Caderno de Esboços", caixaX + 130, itemY + 10);
            g2d.setFont(fontCrayonHand.deriveFont(Font.PLAIN, 14));
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawString("Obtido de: Líder Arte", caixaX + 130, itemY + 35);
            itemCount++;
            itemY += 80;
        }

        if (temCronograma) {
            g2d.setFont(fontCrayonHand.deriveFont(Font.BOLD, 20));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Cronograma de Treinos", caixaX + 130, itemY + 10);
            g2d.setFont(fontCrayonHand.deriveFont(Font.PLAIN, 14));
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawString("Obtido de: Líder Fitness", caixaX + 130, itemY + 35);
            itemCount++;
            itemY += 80;
        }

        if (itemCount == 0) {
            g2d.setFont(fontCrayonHand.deriveFont(Font.PLAIN, 18));
            g2d.setColor(new Color(200, 100, 100));
            g2d.drawString("Inventário vazio", caixaX + 250, itemY + 50);
        }

        g2d.setFont(fontCrayonHand.deriveFont(Font.PLAIN, 16));
        g2d.setColor(new Color(150, 150, 150));
        g2d.drawString("Pressione [B] para fechar o inventário", caixaX + 50, caixaY + caixaAltura - 20);
    }

    private void desenharObjetivos(Graphics2D g2d) {
        int objetivosX = 20;
        int objetivosY = 20;
        int objetivosLargura = 200;
        int objetivosAltura = 180;

        g2d.setColor(new Color(100, 100, 100, 200));
        g2d.fillRoundRect(objetivosX, objetivosY, objetivosLargura, objetivosAltura, 10, 10);

        g2d.setColor(new Color(200, 100, 100));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(objetivosX, objetivosY, objetivosLargura, objetivosAltura, 10, 10);

        g2d.setFont(fontCrayonHand.deriveFont(Font.BOLD, 14));
        g2d.setColor(new Color(255, 150, 150));
        g2d.drawString("OBJETIVOS", objetivosX + 30, objetivosY + 25);

        g2d.setColor(new Color(200, 100, 100));
        g2d.drawLine(objetivosX + 10, objetivosY + 35, objetivosX + objetivosLargura - 10, objetivosY + 35);

        int yOffset = objetivosY + 60;
        g2d.setFont(fontCrayonHand.deriveFont(Font.PLAIN, 11));

        if (explorouCorredor) {
            g2d.setColor(new Color(100, 255, 100));
            g2d.drawString("✓ Explorar corredor", objetivosX + 15, yOffset);
        } else {
            g2d.setColor(new Color(255, 200, 100));
            g2d.drawString("○ Explorar corredor", objetivosX + 15, yOffset);
        }

        yOffset += 25;

        if (temChave) {
            g2d.setColor(new Color(100, 255, 100));
            g2d.drawString("✓ Pegar chave", objetivosX + 15, yOffset);
        } else {
            g2d.setColor(new Color(255, 200, 100));
            g2d.drawString("○ Pegar chave", objetivosX + 15, yOffset);
        }

        yOffset += 25;

        if (livroJoiFoiPego) {
            g2d.setColor(new Color(100, 255, 100));
            g2d.drawString("✓ Abrir armário", objetivosX + 15, yOffset);
        } else {
            g2d.setColor(new Color(255, 200, 100));
            g2d.drawString("○ Abrir armário", objetivosX + 15, yOffset);
        }

        yOffset += 25;

        if (temLivro) {
            g2d.setColor(new Color(100, 255, 100));
            g2d.drawString("✓ Pegar livro", objetivosX + 15, yOffset);
        } else {
            g2d.setColor(new Color(255, 200, 100));
            g2d.drawString("○ Pegar livro", objetivosX + 15, yOffset);
        }

        yOffset += 25;

        if (missaoLeituraAtiva || missaoArteAtiva || missaoFitnessAtiva) {
            g2d.setColor(new Color(255, 200, 100));
            g2d.drawString("○ Abrir o Diário (J)", objetivosX + 15, yOffset);
        }
    }

    private void desenharEfeitoChave(Graphics2D g2d) {
        int x = LARGURA - 120;
        int y = 100;

        float alpha = 1.0f - (contadorEfeitoChave / 60.0f);

        if (alpha > 0) {
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            g2d.setComposite(ac);

            float escala = 1.0f + (contadorEfeitoChave / 30.0f);
            int tamanhoCh = (int) (80 * escala);

            if (imgChave != null) {
                Image chaveEfeito = redimensionarImagem(imgChave, tamanhoCh, tamanhoCh);
                g2d.drawImage(chaveEfeito, x - tamanhoCh / 2, y - tamanhoCh / 2, this);
            }

            g2d.setFont(fontCrayonHand.deriveFont(Font.BOLD, 24));
            g2d.setColor(new Color(255, 215, 0));
            g2d.drawString("COLETADO!", x - 70, y + 100);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    private void desenharInterface(Graphics2D g2d) {
        if (!armarioEstaAberto && textoDialogo.isEmpty()) {
            g2d.setFont(fontCrayonHand.deriveFont(Font.BOLD, 18));

            if (indiceMapa == 0 && Math.abs(audreyX - posArmarioX) < 150) {
                desenharTextoComSombra(g2d, "Pressione [E] para abrir", audreyX - 10, audreyY - 430, Color.CYAN);
            }

            if (indiceMapa == 1 && Math.abs(audreyX - posPortaX) < 150) {
                if (explorouCorredor) {
                    desenharTextoComSombra(g2d, "Pressione [E] para entrar", audreyX - 10, audreyY - 430, Color.GREEN);
                } else {
                    desenharTextoComSombra(g2d, "Vou explorar o final do corredor primeiro...", audreyX - 10,
                            audreyY - 430, Color.LIGHT_GRAY);
                }
            }

            if (indiceMapa == 1 && Math.abs(audreyX - 750) < 150) {
                desenharTextoComSombra(g2d, "Pressione [F] para falar com os alunos", audreyX - 10, audreyY - 430,
                        Color.YELLOW);
            }

            if (indiceMapa == 2 && Math.abs(audreyX - posNicolasXSalaAula) < 120) {
                desenharTextoComSombra(g2d, "Pressione [F] para falar", audreyX - 10, audreyY - 430, Color.YELLOW);
            }

            if (indiceMapa == 2 && estaProximoDaPuerta()) {
                desenharTextoComSombra(g2d, "Pressione [E] para sair", audreyX - 10, audreyY - 430, Color.RED);
            }
        }

        if (!textoDialogo.isEmpty() || armarioEstaAberto) {
            int caixaH = 120;
            int caixaY = ALTURA - caixaH - 20;

            g2d.setColor(new Color(0, 0, 0, 220));
            g2d.fillRoundRect(50, caixaY, 900, caixaH, 20, 20);

            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(50, caixaY, 900, caixaH, 20, 20);

            if (!nomePersonagem.isEmpty() && !armarioEstaAberto) {
                g2d.setFont(fontCrayonHand.deriveFont(Font.BOLD, 16));
                g2d.setColor(new Color(255, 215, 0));
                g2d.drawString(nomePersonagem, 80, caixaY + 25);
            }

            g2d.setFont(fontCrayonHand.deriveFont(Font.BOLD, 18));
            String msg = armarioEstaAberto ? "Pressione 'Q' para fechar o armário." : textoDialogo;
            g2d.setColor(Color.WHITE);
            g2d.drawString(msg, 80, caixaY + 60);

            if (!armarioEstaAberto && !textoDialogo.isEmpty()) {
                g2d.setFont(fontCrayonHand.deriveFont(Font.PLAIN, 13));

                if (estaEmDialogoNicolas && faseDialogoNicolas < 5) {
                    desenharTextoComSombra(g2d, "(Pressione [F] para próxima fala ou [Q] para fechar)", 80,
                            caixaY + 105, Color.LIGHT_GRAY);
                } else {
                    desenharTextoComSombra(g2d, "(Pressione [Q] para fechar)", 80, caixaY + 105, Color.LIGHT_GRAY);
                }
            }
        }
    }

    private void desenharTextoComSombra(Graphics2D g2d, String t, int x, int y, Color c) {
        g2d.setColor(Color.BLACK);
        g2d.drawString(t, x + 2, y + 2);
        g2d.setColor(c);
        g2d.drawString(t, x, y);
    }

    private boolean estaProximoDaPuerta() {
        return indiceMapa == 2 &&
                audreyX < puertaSaidaX + puertaSaidaLargura + 50 &&
                audreyX + AUDREY_LARGURA > puertaSaidaX - 50;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (mostrando_chave) {
            contadorEfeitoChave++;
            if (contadorEfeitoChave >= 60) {
                mostrando_chave = false;
                contadorEfeitoChave = 0;
            }
        }

        if (!armarioEstaAberto) {
            audreyX += velX;

            indiceMapa_public = indiceMapa;
            audreyX_public = audreyX;

            if (indiceMapa == 0) {
                if (audreyX < 0) {
                    audreyX = 0;
                }
                if (audreyX > LARGURA) {
                    indiceMapa = 1;
                    audreyX = 0;
                    textoDialogo = "";
                    nomePersonagem = "";
                    estaEmDialogoNicolas = false;
                    faseDialogoNicolas = 0;
                }
            } else if (indiceMapa == 1) {
                if (audreyX > 700) {
                    explorouCorredor = true;
                }
                if (audreyX < 0) {
                    audreyX = 0;
                    if (velX < 0) {
                        indiceMapa = 0;
                        audreyX = LARGURA - AUDREY_LARGURA;
                        textoDialogo = "";
                        nomePersonagem = "";
                        estaEmDialogoNicolas = false;
                        faseDialogoNicolas = 0;
                    }
                }
                if (audreyX + AUDREY_LARGURA > LARGURA) {
                    audreyX = LARGURA - AUDREY_LARGURA;
                }
            } else if (indiceMapa == 2) {
                if (!cutsceneSalaVista) {
                    cutsceneSalaVista = true;
                    velX = 0;
                    estaMovendo = false;
                    frame.irParaCutscene(1);
                    return;
                }

                if (audreyX < 0) {
                    audreyX = 0;
                }
                if (audreyX + AUDREY_LARGURA > LARGURA) {
                    audreyX = LARGURA - AUDREY_LARGURA;
                }
            }

            if (indiceMapa == 2) {
                // Checar distância dos 3 grupos e esconder diálogo se longe
                if (Math.abs(audreyX - 300) > 150 && Math.abs(audreyX - 550) > 150 && Math.abs(audreyX - 800) > 150) {
                    if (!estaEmDialogoNicolas) {
                        textoDialogo = "";
                        nomePersonagem = "";
                    }
                }
            }

            if (indiceMapa == 0 && Math.abs(audreyX - posArmarioX) > 200) {
                textoDialogo = "";
                nomePersonagem = "";
            }

            if (indiceMapa == 1 && Math.abs(audreyX - posPortaX) > 200 && Math.abs(audreyX - 750) > 200) {
                textoDialogo = "";
                nomePersonagem = "";
            }

            if (estaMovendo) {
                contadorAnimacao++;
                if (contadorAnimacao > 10) {
                    frameAtual = (frameAtual == 0) ? 1 : 0;
                    contadorAnimacao = 0;
                }
            } else {
                frameAtual = 0;
            }
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_ESCAPE) {
            frame.mostrarMenuEmJogo();
            return;
        }

        if (code == KeyEvent.VK_B) {
            inventarioAberto = !inventarioAberto;
            return;
        }

        // M para mostrar/esconder objetivos
        if (code == KeyEvent.VK_M) {
            mostrarObjetivos = !mostrarObjetivos;
            return;
        }

        if (code == KeyEvent.VK_D) {
            velX = 10;
            olhandoDireita = true;
            estaMovendo = true;
        }

        if (code == KeyEvent.VK_A) {
            velX = -10;
            olhandoDireita = false;
            estaMovendo = true;
        }

        if (code == KeyEvent.VK_J) {
            diarioAberto = !diarioAberto;
            return;
        }

        if (code == KeyEvent.VK_F) {
            if (indiceMapa == 1) {
                if (Math.abs(audreyX - 750) < 150) {
                    GerenciadorAudio.tocarSomDialogo();
                    estaEmDialogoNicolas = true;
                    nomePersonagem = "Alunos do Corredor";
                    textoDialogo = "Você é a aluna nova? A sala de aula do seu ano fica na porta atrás de você!";
                    explorouCorredor = true; // Agora faz sentido a exploração
                    return;
                }
            }

            if (indiceMapa == 2) {
                // Checa npc Leitura (x=300)
                if (Math.abs(audreyX - 300) < 120) {
                    estaEmDialogoNicolas = true; // reaproveitando a flag de estar em dialogo
                    nomePersonagem = "Garoto com Livro";
                    if (!missaoLeituraAtiva && !missaoLeituraConcluida) {
                        if (faseDialogoLeitura == 0) {
                            GerenciadorAudio.tocarSomDialogo();
                            textoDialogo = "Você deve ser a Audrey, a aluna nova. O segredo de uma mente afiada é a constância.";
                            faseDialogoLeitura = 1;
                        } else if (faseDialogoLeitura == 1) {
                            textoDialogo = "Toma aqui esse marcador de páginas e a chave do armário.";
                            temChave = true;
                            faseDialogoLeitura = 2;
                        } else if (faseDialogoLeitura == 2) {
                            textoDialogo = "Assim que você pegar o livro e ler um capítulo na vida real...";
                            faseDialogoLeitura = 3;
                        } else if (faseDialogoLeitura == 3) {
                            textoDialogo = "abra o seu diário e marque como concluído aqui no jogo.";
                            missaoLeituraAtiva = true;
                            faseDialogoLeitura = 4;
                        }
                    } else if (missaoLeituraConcluida) {
                        textoDialogo = "Incrível! Sua mente está mais afiada do que nunca!";
                        verificarLevel3();
                    } else {
                        textoDialogo = "Vá ler seu livro e marque no Diário (J)!";
                    }
                }

                // Checa npc Arte (x=550)
                else if (Math.abs(audreyX - 550) < 120) {
                    estaEmDialogoNicolas = true;
                    nomePersonagem = "Estudante Artista";
                    if (!missaoArteAtiva && !missaoArteConcluida) {
                        if (faseDialogoArte == 0) {
                            GerenciadorAudio.tocarSomDialogo();
                            textoDialogo = "Oi Audrey! O segredo da verdadeira expressão é a prática diária.";
                            faseDialogoArte = 1;
                        } else if (faseDialogoArte == 1) {
                            textoDialogo = "Toma aqui esse caderno de esboços.";
                            temCadernoEsboco = true;
                            faseDialogoArte = 2;
                        } else if (faseDialogoArte == 2) {
                            textoDialogo = "Recrie o traço de uma obra famosa na vida real...";
                            faseDialogoArte = 3;
                        } else if (faseDialogoArte == 3) {
                            textoDialogo = "depois abra o seu diário e marque como concluído aqui no jogo.";
                            missaoArteAtiva = true;
                            faseDialogoArte = 4;
                        }
                    } else if (missaoArteConcluida) {
                        textoDialogo = "Seu traço está magnífico! Continue assim!";
                        verificarLevel3();
                    } else {
                        textoDialogo = "Vá fazer seu desenho e marque no Diário (J)!";
                    }
                }

                // Checa npc Fitness (x=800)
                else if (Math.abs(audreyX - 800) < 120) {
                    estaEmDialogoNicolas = true;
                    nomePersonagem = "Atleta da Turma";
                    if (!missaoFitnessAtiva && !missaoFitnessConcluida) {
                        if (faseDialogoFitness == 0) {
                            GerenciadorAudio.tocarSomDialogo();
                            textoDialogo = "E aí Audrey! Preparada? A chave de ouro é a disciplina.";
                            faseDialogoFitness = 1;
                        } else if (faseDialogoFitness == 1) {
                            textoDialogo = "Toma aqui esse cronograma de treinos.";
                            temCronograma = true;
                            faseDialogoFitness = 2;
                        } else if (faseDialogoFitness == 2) {
                            textoDialogo = "Sempre que você fizer 12 polichinelos na sua vida real...";
                            faseDialogoFitness = 3;
                        } else if (faseDialogoFitness == 3) {
                            textoDialogo = "abre o seu diário e marca como concluído. Seja honesta!";
                            missaoFitnessAtiva = true;
                            faseDialogoFitness = 4;
                        }
                    } else if (missaoFitnessConcluida) {
                        textoDialogo = "Sua energia é contagiante! Parabéns!";
                        verificarLevel3();
                    } else {
                        textoDialogo = "Faça seus 12 polichinelos e marque no Diário (J)!";
                    }
                }
            }
        }

        if (code == KeyEvent.VK_E) {
            // Pegar livro no armário
            if (armarioEstaAberto && !livroJoiFoiPego) {
                GerenciadorAudio.tocarSomColeta();
                temLivro = true;
                livroJoiFoiPego = true;
                return;
            }

            // Sair da sala de aula
            if (indiceMapa == 2 && estaProximoDaPuerta()) {
                GerenciadorAudio.tocarSomPortaMadeira();
                indiceMapa = 1;
                audreyX = ultimaPosAoEntraSala;
                textoDialogo = "";
                nomePersonagem = "";
                estaEmDialogoNicolas = false;
                faseDialogoNicolas = 0;
            }
            // Entrar na sala de aula
            else if (indiceMapa == 1 && Math.abs(audreyX - posPortaX) < 150) {
                if (explorouCorredor) {
                    GerenciadorAudio.tocarSomPortaMadeira();
                    ultimaPosAoEntraSala = audreyX;
                    indiceMapa = 2;
                    audreyX = 100;
                    textoDialogo = "";
                    nomePersonagem = "";
                    estaEmDialogoNicolas = false;
                    faseDialogoNicolas = 0;
                } else {
                    nomePersonagem = "Audrey";
                    textoDialogo = "Ainda não me sinto pronta para entrar... vou dar uma olhada no final do corredor primeiro.";
                }
            }
            // Abrir armário
            else if (indiceMapa == 0 && Math.abs(audreyX - posArmarioX) < 150 && textoDialogo.isEmpty()) {
                nomePersonagem = "Audrey";

                if (temChave) {
                    GerenciadorAudio.tocarSomArmario();
                    textoDialogo = "Destrancado! Vamos ver o que tem aqui...";
                    armarioEstaAberto = true;
                    temChave = false;
                } else if (!livroJoiFoiPego) {
                    GerenciadorAudio.tocarSomErro();
                    if (missaoLeituraAtiva) {
                        textoDialogo = "Está trancado... o garoto com o livro me deu a chave, mas ainda não encontrei!";
                    } else if (faseDialogoLeitura > 0) {
                        textoDialogo = "Está trancado... preciso continuar falando com o garoto do livro para pegar a chave.";
                    } else {
                        textoDialogo = "Está trancado. Não tenho a chave...";
                    }
                } else {
                    GerenciadorAudio.tocarSomArmario();
                    textoDialogo = "Vamos ver o que tem aqui...";
                    armarioEstaAberto = true;
                }
                estaEmDialogoNicolas = false;
            }
        }

        if (code == KeyEvent.VK_Q) {
            if (armarioEstaAberto) {
                armarioEstaAberto = false;
            }
            textoDialogo = "";
            nomePersonagem = "";
            estaEmDialogoNicolas = false;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        velX = 0;
        estaMovendo = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    private void verificarLevel3() {
        if (nivelLeitura == 3 && nivelArte == 3 && nivelFitness == 3) {
            frame.irParaCutscene(2); // Cutscene final
        }
    }

    private void desenharDiario(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, LARGURA, ALTURA);

        int diarioX = 100;
        int diarioY = 50;
        int diarioW = 800;
        int diarioH = 650;

        // Fundo do diário
        g2d.setColor(new Color(255, 250, 240));
        g2d.fillRoundRect(diarioX, diarioY, diarioW, diarioH, 30, 30);
        g2d.setColor(new Color(200, 180, 150));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRoundRect(diarioX, diarioY, diarioW, diarioH, 30, 30);
        g2d.drawLine(diarioX + diarioW / 2, diarioY, diarioX + diarioW / 2, diarioY + diarioH);

        g2d.setFont(fontCrayonHand.deriveFont(Font.BOLD, 40));
        g2d.setColor(new Color(100, 50, 50));
        g2d.drawString("Meu Diário", diarioX + 80, diarioY + 60);

        g2d.setFont(fontCrayonHand.deriveFont(Font.PLAIN, 20));

        int textX = diarioX + 40;
        int textY = diarioY + 120;

        g2d.drawString("Leitura (Nível " + nivelLeitura + ")", textX, textY);
        if (missaoLeituraAtiva && !missaoLeituraConcluida) {
            g2d.setColor(Color.RED);
            g2d.drawString("Ler um livro na VIDA REAL", textX + 20, textY + 30);
            g2d.drawRect(textX + 300, textY + 5, 30, 30); // Checkbox
            g2d.setColor(new Color(100, 50, 50));
        } else if (missaoLeituraConcluida) {
            g2d.setColor(new Color(50, 150, 50));
            g2d.drawString("Ler um livro (Concluído!)", textX + 20, textY + 30);
            g2d.setColor(new Color(100, 50, 50));
        } else {
            g2d.drawString("Fale com o grupo de Leitura", textX + 20, textY + 30);
        }

        textY += 100;
        g2d.drawString("Arte (Nível " + nivelArte + ")", textX, textY);
        if (missaoArteAtiva && !missaoArteConcluida) {
            g2d.setColor(Color.RED);
            g2d.drawString("Recriar obra na VIDA REAL", textX + 20, textY + 30);
            g2d.drawRect(textX + 300, textY + 5, 30, 30);
            g2d.setColor(new Color(100, 50, 50));
        } else if (missaoArteConcluida) {
            g2d.setColor(new Color(50, 150, 50));
            g2d.drawString("Recriar obra (Concluído!)", textX + 20, textY + 30);
            g2d.setColor(new Color(100, 50, 50));
        } else {
            g2d.drawString("Fale com o grupo de Arte", textX + 20, textY + 30);
        }

        textY += 100;
        g2d.drawString("Fitness (Nível " + nivelFitness + ")", textX, textY);
        if (missaoFitnessAtiva && !missaoFitnessConcluida) {
            g2d.setColor(Color.RED);
            g2d.drawString("12 polichinelos na VIDA REAL", textX + 20, textY + 30);
            g2d.drawRect(textX + 300, textY + 5, 30, 30);
            g2d.setColor(new Color(100, 50, 50));
        } else if (missaoFitnessConcluida) {
            g2d.setColor(new Color(50, 150, 50));
            g2d.drawString("12 polichinelos (Concluído!)", textX + 20, textY + 30);
            g2d.setColor(new Color(100, 50, 50));
        } else {
            g2d.drawString("Fale com o grupo Fitness", textX + 20, textY + 30);
        }

        // Custom missions
        int rightX = diarioX + diarioW / 2 + 40;
        int rightY = diarioY + 80;
        g2d.drawString("Missões Livres (Level 3)", rightX, rightY);

        if (missaoLeituraConcluida && missaoArteConcluida && missaoFitnessConcluida) {
            g2d.setFont(fontCrayonHand.deriveFont(Font.PLAIN, 16));
            g2d.drawString("Agora você cria suas próprias missões!", rightX, rightY + 30);

            if (nivelLeitura < 3) {
                g2d.setColor(Color.BLUE);
                g2d.drawString("Criar e Concluir Missão Leitura", rightX, rightY + 80);
                g2d.drawRect(rightX + 280, rightY + 60, 30, 30);
            } else {
                g2d.setColor(new Color(50, 150, 50));
                g2d.drawString("Leitura Level 3 Atingido!", rightX, rightY + 80);
            }

            if (nivelArte < 3) {
                g2d.setColor(Color.BLUE);
                g2d.drawString("Criar e Concluir Missão Arte", rightX, rightY + 130);
                g2d.drawRect(rightX + 280, rightY + 110, 30, 30);
            } else {
                g2d.setColor(new Color(50, 150, 50));
                g2d.drawString("Arte Level 3 Atingido!", rightX, rightY + 130);
            }

            if (nivelFitness < 3) {
                g2d.setColor(Color.BLUE);
                g2d.drawString("Criar e Concluir Missão Fitness", rightX, rightY + 180);
                g2d.drawRect(rightX + 280, rightY + 160, 30, 30);
            } else {
                g2d.setColor(new Color(50, 150, 50));
                g2d.drawString("Fitness Level 3 Atingido!", rightX, rightY + 180);
            }
        } else {
            g2d.setFont(fontCrayonHand.deriveFont(Font.PLAIN, 16));
            g2d.drawString("(Complete as missões iniciais primeiro)", rightX, rightY + 30);
        }

        g2d.setColor(new Color(100, 50, 50));
        g2d.setFont(fontCrayonHand.deriveFont(Font.PLAIN, 16));
        g2d.drawString("Marque as caixas apenas DEPOIS de concluir na VIDA REAL!", diarioX + 40,
                diarioY + diarioH - 50);
        g2d.drawString("Pressione [J] para fechar", diarioX + diarioW / 2 - 100, diarioY + diarioH - 20);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!diarioAberto)
            return;

        int w = getWidth();
        int h = getHeight();
        double scaleX = (double) w / LARGURA;
        double scaleY = (double) h / ALTURA;
        double scale = Math.min(scaleX, scaleY);
        int xOffset = (int) ((w - (LARGURA * scale)) / 2);
        int yOffset = (int) ((h - (ALTURA * scale)) / 2);

        int realX = (int) ((e.getX() - xOffset) / scale);
        int realY = (int) ((e.getY() - yOffset) / scale);

        int diarioX = 100;
        int diarioY = 50;
        int textX = diarioX + 40;

        // Leitura Level 2
        if (missaoLeituraAtiva && !missaoLeituraConcluida) {
            if (realX >= textX + 300 && realX <= textX + 330 && realY >= diarioY + 120 + 5
                    && realY <= diarioY + 120 + 35) {
                missaoLeituraConcluida = true;
                nivelLeitura = 2;
                GerenciadorAudio.tocarSomColeta();
                verificarLevel3();
            }
        }
        // Arte Level 2
        if (missaoArteAtiva && !missaoArteConcluida) {
            if (realX >= textX + 300 && realX <= textX + 330 && realY >= diarioY + 220 + 5
                    && realY <= diarioY + 220 + 35) {
                missaoArteConcluida = true;
                nivelArte = 2;
                GerenciadorAudio.tocarSomColeta();
                verificarLevel3();
            }
        }
        // Fitness Level 2
        if (missaoFitnessAtiva && !missaoFitnessConcluida) {
            if (realX >= textX + 300 && realX <= textX + 330 && realY >= diarioY + 320 + 5
                    && realY <= diarioY + 320 + 35) {
                missaoFitnessConcluida = true;
                nivelFitness = 2;
                GerenciadorAudio.tocarSomColeta();
                verificarLevel3();
            }
        }

        // Missões Level 3
        if (missaoLeituraConcluida && missaoArteConcluida && missaoFitnessConcluida) {
            int rightX = diarioX + 800 / 2 + 40;
            int rightY = diarioY + 80;

            if (nivelLeitura < 3) {
                if (realX >= rightX + 280 && realX <= rightX + 310 && realY >= rightY + 60 && realY <= rightY + 90) {
                    nivelLeitura = 3;
                    GerenciadorAudio.tocarSomColeta();
                    verificarLevel3();
                }
            }
            if (nivelArte < 3) {
                if (realX >= rightX + 280 && realX <= rightX + 310 && realY >= rightY + 110 && realY <= rightY + 140) {
                    nivelArte = 3;
                    GerenciadorAudio.tocarSomColeta();
                    verificarLevel3();
                }
            }
            if (nivelFitness < 3) {
                if (realX >= rightX + 280 && realX <= rightX + 310 && realY >= rightY + 160 && realY <= rightY + 190) {
                    nivelFitness = 3;
                    GerenciadorAudio.tocarSomColeta();
                    verificarLevel3();
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

}

class MenuSlots extends JPanel {
    private JogoAudrey frame;
    private int acaoAtual;
    private JButton btnSlot1, btnSlot2, btnSlot3, btnVoltar;
    private JButton[] btnApagarSlots = new JButton[3];
    private Font fontCrayonHand, fontTitulo;
    private Color corPrincipal = new Color(255, 230, 235); // Rosa pastel claro

    public MenuSlots(JogoAudrey frame) {
        this.frame = frame;
        setBackground(new Color(255, 245, 235)); // Creme pastel
        setLayout(null);
        carregarFonts();
        criarComponentes();
    }

    private void carregarFonts() {
        try {
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf"))
                    .deriveFont(28f);
            fontTitulo = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf"))
                    .deriveFont(60f);
        } catch (Exception e) {
            fontCrayonHand = new Font("Arial", Font.BOLD, 28);
            fontTitulo = new Font("Arial", Font.BOLD, 60);
        }
    }

    private void criarComponentes() {
        btnSlot1 = criarBotao("SLOT 1");
        btnSlot1.addActionListener(e -> selecionarSlot(1));
        add(btnSlot1);
        btnApagarSlots[0] = criarBotaoLixeira(1);
        add(btnApagarSlots[0]);

        btnSlot2 = criarBotao("SLOT 2");
        btnSlot2.addActionListener(e -> selecionarSlot(2));
        add(btnSlot2);
        btnApagarSlots[1] = criarBotaoLixeira(2);
        add(btnApagarSlots[1]);

        btnSlot3 = criarBotao("SLOT 3");
        btnSlot3.addActionListener(e -> selecionarSlot(3));
        add(btnSlot3);
        btnApagarSlots[2] = criarBotaoLixeira(3);
        add(btnApagarSlots[2]);

        btnVoltar = criarBotao("VOLTAR");
        btnVoltar.addActionListener(e -> voltar());
        add(btnVoltar);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int w = getWidth();
        int h = getHeight();
        int bw = Math.max(310, w / 3);
        int bh = Math.max(80, h / 9);
        int bx = (w - bw) / 2;
        int startY = (int) (h * 0.25);
        int gap = (int) (h * 0.13);

        JButton[] btns = { btnSlot1, btnSlot2, btnSlot3 };
        for (int i = 0; i < btns.length; i++) {
            if (btns[i] != null) {
                btns[i].setBounds(bx, startY + i * gap, bw, bh);
                if (btnApagarSlots[i] != null) {
                    btnApagarSlots[i].setBounds(bx + bw + 10, startY + i * gap, bh, bh); // lixeira ao lado, do tamanho
                                                                                         // da altura do botão
                }
            }
        }
        if (btnVoltar != null) {
            btnVoltar.setBounds((w - 400) / 2, startY + 3 * gap + 20, 400, 70); // voltar em baixo
        }
    }

    private JButton criarBotaoLixeira(int slot) {
        JButton botao = new JButton("🗑️") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                int r = Math.min(w, h) - 10;
                boolean isHovered = getModel().isRollover();
                boolean isPressed = getModel().isPressed();
                int offsetY = isPressed ? 4 : 0;

                if (!isPressed) {
                    g2d.setColor(new Color(180, 80, 80, 50));
                    g2d.fillRoundRect(0, 4, w, h - 4, r, r);
                }
                g2d.setColor(isPressed ? new Color(255, 140, 140)
                        : isHovered ? new Color(255, 160, 160) : new Color(255, 200, 200));
                g2d.fillRoundRect(0, offsetY, w, h - 4, r, r);

                FontMetrics fm = g2d.getFontMetrics(getFont());
                int tx = (w - fm.stringWidth(getText())) / 2;
                int ty = offsetY + (h - 4 - fm.getHeight()) / 2 + fm.getAscent();
                g2d.setColor(new Color(180, 80, 80));
                g2d.drawString(getText(), tx, ty);
                g2d.dispose();
            }
        };
        botao.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 34));
        botao.setContentAreaFilled(false);
        botao.setBorderPainted(false);
        botao.setFocusPainted(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));

        botao.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this, "Deseja REALMENTE apagar o save do Slot " + slot + "?\nEsta ação não pode ser desfeita.",
                    "Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                Database.apagarSave(slot);
                JOptionPane.showMessageDialog(this, "Save apagado com sucesso.");
                atualizarBotoes();
                if (frame.getSlotAtual() == slot)
                    frame.setSlotAtual(-1);
                frame.getMenuPrincipal().atualizarBotoes();
            }
        });
        return botao;
    }

    public void preparar(int acao) {
        this.acaoAtual = acao;
        atualizarBotoes();
    }

    private void atualizarBotoes() {
        JButton[] botoes = { btnSlot1, btnSlot2, btnSlot3 };
        for (int i = 0; i < 3; i++) {
            int slot = i + 1;
            boolean existe = Database.saveExiste(slot);
            String texto = "SLOT " + slot + (existe ? " (Ocupado)" : " (Vazio)");
            botoes[i].setText(texto);

            if (acaoAtual == JogoAudrey.ACAO_CONTINUAR || acaoAtual == JogoAudrey.ACAO_APAGAR) {
                botoes[i].setEnabled(existe);
            } else {
                botoes[i].setEnabled(true);
            }

            if (btnApagarSlots[i] != null) {
                btnApagarSlots[i].setVisible(existe); // Só mostra lixeira se existir save nesse slot
            }
        }
    }

    private void voltar() {
        if (acaoAtual == JogoAudrey.ACAO_SALVAR) {
            frame.mostrarMenuEmJogo();
        } else {
            frame.mostrarMenuPrincipal();
        }
    }

    private void selecionarSlot(int slot) {
        boolean ocupado = Database.saveExiste(slot);

        switch (acaoAtual) {
            case JogoAudrey.ACAO_NOVO:
                if (ocupado) {
                    int confirm = JOptionPane.showConfirmDialog(
                            this,
                            "O Slot " + slot + " já possui um save.\nDeseja realmente apagar e iniciar um novo jogo?",
                            "Aviso", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION)
                        return;
                    Database.apagarSave(slot);
                }
                frame.iniciarJogo(slot);
                break;

            case JogoAudrey.ACAO_CONTINUAR:
                if (ocupado) {
                    frame.continuarJogoSalvo(slot);
                }
                break;

            case JogoAudrey.ACAO_APAGAR:
                if (ocupado) {
                    int confirm = JOptionPane.showConfirmDialog(
                            this,
                            "ATENÇÃO: Deseja REALMENTE apagar o save do Slot " + slot
                                    + "?\nEsta ação não pode ser desfeita.",
                            "Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                        Database.apagarSave(slot);
                        JOptionPane.showMessageDialog(this, "Save apagado com sucesso.");
                        atualizarBotoes();
                    }
                }
                break;

            case JogoAudrey.ACAO_SALVAR:
                if (ocupado && slot != frame.getSlotAtual()) {
                    int confirm = JOptionPane.showConfirmDialog(
                            this, "O Slot " + slot + " já possui um save.\nDeseja realmente sobrescrevê-lo?",
                            "Aviso", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION)
                        return;
                }
                frame.setSlotAtual(slot);
                frame.getJogoPanel().salvarEstado(slot);
                JOptionPane.showMessageDialog(this, "Jogo salvo com sucesso no Slot " + slot + "!");
                voltar();
                break;
        }
    }

    private JButton criarBotao(String texto) {
        return new BotaoEstilizado(texto, fontCrayonHand);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(255, 245, 235), // Creme pastel
                0, getHeight(), new Color(255, 230, 240) // Rosa pastel
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setFont(fontTitulo);
        String titulo = "";
        if (acaoAtual == JogoAudrey.ACAO_NOVO)
            titulo = "NOVO JOGO";
        else if (acaoAtual == JogoAudrey.ACAO_CONTINUAR)
            titulo = "CONTINUAR";
        else if (acaoAtual == JogoAudrey.ACAO_APAGAR)
            titulo = "APAGAR SAVE";
        else if (acaoAtual == JogoAudrey.ACAO_SALVAR)
            titulo = "SALVAR JOGO";

        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(titulo)) / 2;

        g2d.setColor(new Color(200, 150, 180, 120)); // Sombra rosa pastel
        g2d.drawString(titulo, x + 4, 124);

        GradientPaint titleGrad = new GradientPaint(
                x, 70, new Color(255, 150, 180), // Rosa forte
                x, 130, new Color(180, 230, 180) // Verde claro
        );
        g2d.setPaint(titleGrad);
        g2d.drawString(titulo, x, 120);
    }
}

class BotaoEstilizado extends JButton {
    private Color corFundoNormal = new Color(255, 230, 235);
    private Color corFundoHover = new Color(255, 210, 220);
    private Color corFundoPress = new Color(255, 190, 205);
    private Color corBordaNormal = new Color(180, 230, 180);
    private Color corBordaHover = new Color(150, 210, 150);
    private Color corTexto = new Color(120, 80, 100);
    private Font fonteBase;

    public BotaoEstilizado(String texto, Font fonte) {
        super(texto);
        this.fonteBase = fonte;
        setFont(fonte);
        setForeground(corTexto);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled())
                    setFont(fonteBase.deriveFont(Font.BOLD));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled())
                    setFont(fonteBase);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int r = Math.min(w, h) - 10; // Formato pílula (bem arredondado)

        boolean isHovered = getModel().isRollover();
        boolean isPressed = getModel().isPressed();
        boolean isEnabled = isEnabled();

        Color corFundo = !isEnabled ? new Color(240, 220, 225)
                : isPressed ? corFundoPress : isHovered ? corFundoHover : corFundoNormal;

        int offsetY = isPressed ? 4 : 0;

        // Sombra / Base 3D
        if (!isPressed && isEnabled) {
            g2d.setColor(new Color(150, 100, 120, 50));
            g2d.fillRoundRect(0, 5, w, h - 5, r, r);
        }

        // Fundo principal
        g2d.setColor(corFundo);
        g2d.fillRoundRect(0, offsetY, w, h - 5, r, r);

        // Borda
        Color corBorda = !isEnabled ? Color.LIGHT_GRAY : isHovered ? corBordaHover : corBordaNormal;
        g2d.setColor(corBorda);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(1, offsetY + 1, w - 3, h - 7, r, r);

        // Texto
        FontMetrics fm = g2d.getFontMetrics(getFont());
        int tx = (w - fm.stringWidth(getText())) / 2;
        int ty = offsetY + (h - 5 - fm.getHeight()) / 2 + fm.getAscent();

        g2d.setColor(!isEnabled ? Color.GRAY : corTexto);
        g2d.drawString(getText(), tx, ty);

        g2d.dispose();
    }
}
