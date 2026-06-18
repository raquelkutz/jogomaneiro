import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;

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

    private ConfiguracoesPanel configuracoesPanel;

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
        configuracoesPanel = new ConfiguracoesPanel(this);

        mainPanel.add(telaCarregamento, "carregamento");
        mainPanel.add(cutscenePanel, "cutscene");
        mainPanel.add(menuPrincipal, "menuPrincipal");
        mainPanel.add(jogoPanel, "jogo");
        mainPanel.add(menuEmJogo, "menuEmJogo");
        mainPanel.add(menuSlots, "menuSlots");
        mainPanel.add(configuracoesPanel, "configuracoes");

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);

        cardLayout.show(mainPanel, "menuPrincipal");
        GerenciadorAudio.tocarMusicaFundo();

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == Configuracoes.getInstance().getTecla("MENU")) {
                    if (menuEmJogo != null && menuEmJogo.isShowing()) {
                        continuarJogo();
                        return true;
                    } else if (jogoPanel != null && jogoPanel.isShowing()) {
                        mostrarMenuEmJogo();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void iniciarJogo(int slot) {
        this.slotAtual = slot;
        recriarJogo(); // Garante que o painel será zerado para um novo jogo
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
        GerenciadorAudio.pausarMusicaFundo();
        cardLayout.show(mainPanel, "menuEmJogo");
    }

    public void mostrarConfiguracoes(String origem) {
        configuracoesPanel.setOrigem(origem);
        cardLayout.show(mainPanel, "configuracoes");
    }

    public void continuarJogo() {
        GerenciadorAudio.retomarMusicaFundo();
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
                telaCarregamento.iniciarCarregamentoContinuar(props);
                cardLayout.show(mainPanel, "carregamento");
            } else {
                cardLayout.show(mainPanel, "jogo");
                jogoPanel.requestFocus();
            }
        }
    }

    public void voltarAoMenuPrincipal() {
        if (jogoIniciado && jogoPanel != null && slotAtual != -1) {
            jogoPanel.salvarEstado(slotAtual);
        }
        jogoIniciado = false;
        slotAtual = -1;
        GerenciadorAudio.retomarMusicaFundo();
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
    private boolean modoContinuar = false;
    private java.util.Properties continuarProps;

    public TelaCarregamento(JogoAudrey frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(Color.BLACK);
        carregarFonts();
        timerLoad = new Timer(20, this);
    }

    public void iniciarCarregamento() {
        modoContinuar = false;
        continuarProps = null;
        progresso = 0;
        anguloAnimacao = 0;
        timerLoad.restart();
    }

    public void iniciarCarregamentoContinuar(java.util.Properties props) {
        this.modoContinuar = true;
        this.continuarProps = props;
        progresso = 0;
        anguloAnimacao = 0;
        timerLoad.restart();
    }

    private void carregarFonts() {
        try {
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf"))
                    .deriveFont(28f);
            fontTitulo = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("KGSecondChancesSketch.ttf"))
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
            if (modoContinuar && continuarProps != null) {
                frame.getJogoPanel().carregarEstado(continuarProps);
                modoContinuar = false;
                continuarProps = null;
                frame.continuarJogo();
            } else {
                frame.irParaCutscene(0);
            }
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

class MenuPrincipal extends JPanel implements ActionListener {
    private JogoAudrey frame;
    private JButton btnPlay, btnContinuar, btnConfig, btnSobre, btnSair;
    private Font fontCrayonHand, fontTitulo;
    private Color corPrincipal;
    private float animAngulo = 0;
    private Timer animTimer;

    public MenuPrincipal(JogoAudrey frame) {
        this.frame = frame;
        corPrincipal = new Color(255, 230, 235);
        setBackground(new Color(255, 245, 235));
        setLayout(null);

        carregarFonts();
        criarComponentes();
        animTimer = new Timer(30, this);
        animTimer.start();
    }

    private void carregarFonts() {
        try {
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf"))
                    .deriveFont(24f);
            fontTitulo = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("KGSecondChancesSketch.ttf"))
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

        btnConfig = criarBotao("CONFIGURACOES");
        btnConfig.addActionListener(e -> frame.mostrarConfiguracoes("menuPrincipal"));
        add(btnConfig);

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
        int bh = Math.max(55, h / 12);
        int bx = (w - bw) / 2;
        int startY = (int) (h * 0.32);
        int gap = (int) (h * 0.10);
        JButton[] btns = { btnPlay, btnContinuar, btnConfig, btnSobre, btnSair };
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
    public void actionPerformed(ActionEvent e) {
        animAngulo += 0.04f;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int W = getWidth(), H = getHeight();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fundo gradiente creme para rosa
        GradientPaint gradient = new GradientPaint(0, 0, new Color(255, 245, 235), W, H, new Color(255, 230, 240));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, W, H);

        // Manchas de cor pastel (verde, amarelo, azul, lavanda)
        g2d.setColor(new Color(200, 240, 210, 40));
        g2d.fillOval(-100, -100, W/2, H/2);
        g2d.setColor(new Color(255, 250, 200, 35));
        g2d.fillOval(W*2/3, H/3, W/2, H/2);
        g2d.setColor(new Color(200, 220, 255, 30));
        g2d.fillOval(W/3, H*2/3, W/2, H/3);
        g2d.setColor(new Color(230, 210, 255, 30));
        g2d.fillOval(W/4, H/4, W/3, H/3);

        desenharCantosDecorativos(g2d, W, H);
        desenharParticulasFundo(g2d, W, H);
        desenharRabiscosMenu(g2d, W, H);

        // Banner decorativo atrás do título
        int titleY = (int) (H * 0.20);
        g2d.setFont(fontTitulo);
        String titulo = "Hobby Quest";
        FontMetrics fm = g2d.getFontMetrics();
        int tx = (W - fm.stringWidth(titulo)) / 2;
        int bannerW = fm.stringWidth(titulo) + 100;
        int bannerH = fm.getHeight() + 40;
        int bannerX = (W - bannerW) / 2;
        int bannerY = titleY - fm.getAscent() - 15;

        // Sombra do banner
        g2d.setColor(new Color(180, 200, 180, 60));
        g2d.fillRoundRect(bannerX + 5, bannerY + 5, bannerW, bannerH, 35, 35);
        // Banner gradiente pastel
        GradientPaint bannerGrad = new GradientPaint(bannerX, bannerY, new Color(255, 255, 245, 240), bannerX, bannerY + bannerH, new Color(240, 255, 245, 240));
        g2d.setPaint(bannerGrad);
        g2d.fillRoundRect(bannerX, bannerY, bannerW, bannerH, 35, 35);
        g2d.setColor(new Color(180, 230, 180, 160));
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawRoundRect(bannerX, bannerY, bannerW, bannerH, 35, 35);

        // Glow externo do título
        g2d.setFont(fontTitulo.deriveFont(Font.BOLD, 74f));
        fm = g2d.getFontMetrics();
        tx = (W - fm.stringWidth(titulo)) / 2;

        g2d.setColor(new Color(180, 220, 180, 40));
        g2d.drawString(titulo, tx + 8, titleY + 8);
        g2d.drawString(titulo, tx - 8, titleY - 8);
        g2d.setColor(new Color(255, 200, 150, 50));
        g2d.drawString(titulo, tx + 6, titleY - 6);
        g2d.drawString(titulo, tx - 6, titleY + 6);

        g2d.setColor(new Color(180, 210, 180, 70));
        g2d.drawString(titulo, tx + 5, titleY + 5);
        g2d.drawString(titulo, tx - 5, titleY - 5);

        g2d.setColor(new Color(200, 160, 120, 150));
        g2d.drawString(titulo, tx + 3, titleY + 3);

        GradientPaint titleGrad = new GradientPaint(tx, titleY - 60, new Color(255, 180, 100), tx, titleY + 10,
                new Color(100, 200, 160));
        g2d.setPaint(titleGrad);
        g2d.drawString(titulo, tx, titleY);

        // Detalhes decorativos ao redor do título
        float a = animAngulo;
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int starR = 8;
        int esx = tx - 35 + (int)(Math.sin(a * 0.7) * 3);
        int esy = titleY - fm.getAscent()/2 + (int)(Math.cos(a * 0.8) * 3);
        g2d.setColor(new Color(255, 215, 100, 200));
        g2d.drawLine(esx - starR, esy, esx + starR, esy);
        g2d.drawLine(esx, esy - starR, esx, esy + starR);

        int edx = tx + fm.stringWidth(titulo) + 35 + (int)(Math.sin(a * 0.9 + 1) * 3);
        int edy = titleY - fm.getAscent()/2 + (int)(Math.cos(a * 1.0 + 1) * 3);
        g2d.setColor(new Color(100, 200, 180, 200));
        g2d.drawLine(edx - starR, edy, edx + starR, edy);
        g2d.drawLine(edx, edy - starR, edx, edy + starR);

        // Painel translúcido atrás dos botões
        int btnStartY = (int) (H * 0.32);
        int btnEndY = btnStartY + 5 * (int) (H * 0.10) + 20;
        int cardW = Math.max(300, W / 3);
        int cardX = (W - cardW) / 2;
        int cardH = btnEndY - btnStartY + 40;

        g2d.setColor(new Color(255, 255, 245, 90));
        g2d.fillRoundRect(cardX, btnStartY - 20, cardW, cardH, 35, 35);
        g2d.setColor(new Color(180, 230, 180, 60));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(cardX, btnStartY - 20, cardW, cardH, 35, 35);
    }

    private void desenharCantosDecorativos(Graphics2D g2d, int w, int h) {
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int marg = 22, tam = 32;
        Color[] cores = {
            new Color(180, 230, 180, 140),
            new Color(255, 215, 100, 140),
            new Color(255, 180, 200, 140),
            new Color(180, 210, 255, 140)
        };

        g2d.setColor(cores[0]);
        g2d.drawArc(marg, marg, tam*2, tam*2, 180, 90);
        g2d.fillOval(marg - 3, marg - 3, 6, 6);
        g2d.fillOval(marg + tam - 3, marg + tam - 3, 6, 6);

        g2d.setColor(cores[1]);
        g2d.drawArc(w - marg - tam*2, marg, tam*2, tam*2, 270, 90);
        g2d.fillOval(w - marg - 3, marg - 3, 6, 6);
        g2d.fillOval(w - marg - tam - 3, marg + tam - 3, 6, 6);

        g2d.setColor(cores[2]);
        g2d.drawArc(marg, h - marg - tam*2, tam*2, tam*2, 90, 90);
        g2d.fillOval(marg - 3, h - marg - 3, 6, 6);
        g2d.fillOval(marg + tam - 3, h - marg - tam - 3, 6, 6);

        g2d.setColor(cores[3]);
        g2d.drawArc(w - marg - tam*2, h - marg - tam*2, tam*2, tam*2, 0, 90);
        g2d.fillOval(w - marg - 3, h - marg - 3, 6, 6);
        g2d.fillOval(w - marg - tam - 3, h - marg - tam - 3, 6, 6);
    }

    private void desenharParticulasFundo(Graphics2D g2d, int w, int h) {
        float a = animAngulo;
        for (int i = 0; i < 35; i++) {
            float cx = (float) ((Math.sin(i * 3.7 + a * 0.25 + i * 0.3) * 0.5 + 0.5) * w);
            float cy = (float) ((Math.cos(i * 2.1 + a * 0.18 + i * 0.5) * 0.5 + 0.5) * h);
            int sz = (i % 4 == 0) ? 6 : (i % 4 == 1) ? 4 : 3;
            int alpha = 40 + (i % 6) * 25;
            Color cor;
            switch (i % 6) {
                case 0: cor = new Color(255, 200, 220, Math.min(alpha, 160)); break;
                case 1: cor = new Color(200, 235, 200, Math.min(alpha, 140)); break;
                case 2: cor = new Color(200, 215, 255, Math.min(alpha, 150)); break;
                case 3: cor = new Color(255, 240, 180, Math.min(alpha, 150)); break;
                case 4: cor = new Color(235, 210, 255, Math.min(alpha, 140)); break;
                default: cor = new Color(255, 220, 200, Math.min(alpha, 130)); break;
            }

            int tipo = i % 5;
            if (tipo == 0) {
                // Estrelinha de 4 pontas
                int r = sz / 2 + 1;
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.setColor(cor);
                g2d.drawLine((int)(cx - r), (int)cy, (int)(cx + r), (int)cy);
                g2d.drawLine((int)cx, (int)(cy - r), (int)cx, (int)(cy + r));
                g2d.drawLine((int)(cx - r/2), (int)(cy - r/2), (int)(cx + r/2), (int)(cy + r/2));
                g2d.drawLine((int)(cx - r/2), (int)(cy + r/2), (int)(cx + r/2), (int)(cy - r/2));
            } else if (tipo == 1) {
                // Coração miniatura
                int hsz = sz;
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.setColor(cor);
                GeneralPath hp = new GeneralPath();
                hp.moveTo(cx, cy + hsz/3);
                hp.curveTo(cx, cy - hsz/3, cx - hsz, cy - hsz/3, cx - hsz, cy);
                hp.curveTo(cx - hsz, cy + hsz*0.5, cx, cy + hsz, cx, cy + hsz/3);
                hp.curveTo(cx, cy + hsz, cx + hsz, cy + hsz*0.5, cx + hsz, cy);
                hp.curveTo(cx + hsz, cy - hsz/3, cx, cy - hsz/3, cx, cy + hsz/3);
                g2d.draw(hp);
            } else {
                g2d.setColor(cor);
                g2d.fillOval((int)cx, (int)cy, sz, sz);
            }
        }
    }

    private void desenharRabiscosMenu(Graphics2D g2d, int w, int h) {
        Stroke s = new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(s);
        float a = animAngulo;

        // Flor (topo esquerdo)
        mFlor(g2d, new Color(255, 175, 200, 170), (int) (w * 0.09), (int) (h * 0.13 + Math.sin(a) * 6), 22);
        // Estrela 4pts (topo direito)
        mEstrela4(g2d, new Color(255, 215, 100, 170), (int) (w * 0.88 + Math.sin(a * 0.7) * 5), (int) (h * 0.10 + Math.cos(a * 0.8) * 5), 16);
        // Nuvem (esquerda meio)
        mNuvem(g2d, new Color(170, 205, 255, 150), (int) (w * 0.05 + Math.sin(a * 0.5) * 3), (int) (h * 0.42 + Math.cos(a * 0.6) * 4));
        // Coração (direita meio) - pulsando
        float pulse = 1.0f + (float) Math.sin(a * 1.2) * 0.15f;
        mCoracao(g2d, new Color(255, 155, 190, 160), (int) (w * 0.87 + Math.sin(a * 0.9) * 4), (int) (h * 0.50 + Math.cos(a) * 5), (int) (22 * pulse));
        // Laço (baixo esquerdo)
        mLaco(g2d, new Color(255, 185, 210, 160), (int) (w * 0.08 + Math.sin(a * 0.6) * 3), (int) (h * 0.77 + Math.cos(a * 0.7) * 4));
        // Estrela 5pts (baixo direito)
        mEstrela5(g2d, new Color(255, 200, 110, 165), (int) (w * 0.89 + Math.sin(a * 0.8) * 4), (int) (h * 0.80 + Math.cos(a * 0.9) * 5), 20);
        // Espiral (baixo centro-direita)
        mEspiral(g2d, new Color(180, 195, 255, 155), (int) (w * 0.80 + Math.sin(a * 0.4) * 3), (int) (h * 0.20 + Math.cos(a * 0.5) * 4));

        // Folhas decorativas
        int folhaCx = (int) (w * 0.55 + Math.sin(a * 0.3) * 4);
        int folhaCy = (int) (h * 0.15 + Math.cos(a * 0.4) * 4);
        g2d.setColor(new Color(180, 220, 180, 140));
        g2d.drawOval(folhaCx - 4, folhaCy, 12, 7);
        g2d.drawLine(folhaCx - 4, folhaCy + 3, folhaCx + 8, folhaCy + 3);

        int folhaCx2 = (int) (w * 0.28 + Math.sin(a * 0.35 + 1) * 4);
        int folhaCy2 = (int) (h * 0.55 + Math.cos(a * 0.45 + 1) * 4);
        g2d.setColor(new Color(200, 230, 180, 130));
        g2d.drawOval(folhaCx2, folhaCy2 - 4, 10, 6);
        g2d.drawLine(folhaCx2 + 5, folhaCy2 - 4, folhaCx2 + 5, folhaCy2 + 2);

        // Borboleta (centro-direita)
        int borX = (int) (w * 0.70 + Math.sin(a * 0.5 + 2) * 6);
        int borY = (int) (h * 0.65 + Math.cos(a * 0.6 + 2) * 5);
        float asa = 1.0f + (float) Math.sin(a * 2) * 0.2f;
        g2d.setColor(new Color(255, 200, 220, 160));
        int asaW = (int) (14 * asa);
        int asaH = 10;
        g2d.drawOval(borX - asaW, borY - asaH, asaW, asaH);
        g2d.drawOval(borX, borY - asaH, asaW, asaH);
        g2d.drawOval(borX - asaW/2, borY, asaW/2, asaH/2);
        g2d.drawOval(borX + asaW/4, borY, asaW/2, asaH/2);
        g2d.setColor(new Color(255, 180, 200, 180));
        g2d.fillOval(borX - 2, borY - 2, 4, 4);
        // Antenas
        g2d.drawLine(borX - 2, borY - 3, borX - 6, borY - 10);
        g2d.drawLine(borX + 2, borY - 3, borX + 6, borY - 10);

        // --- Ícones de hobbies ---
        // Livro (leitura)
        int livroX = (int) (w * 0.18 + Math.sin(a * 0.5 + 1) * 5);
        int livroY = (int) (h * 0.70 + Math.cos(a * 0.6 + 1) * 5);
        g2d.setColor(new Color(200, 170, 140, 160));
        g2d.fillRoundRect(livroX - 14, livroY - 10, 28, 20, 5, 5);
        g2d.setColor(new Color(160, 130, 100, 180));
        g2d.drawRoundRect(livroX - 14, livroY - 10, 28, 20, 5, 5);
        g2d.drawLine(livroX, livroY - 8, livroX, livroY + 8);
        g2d.drawLine(livroX + 3, livroY - 5, livroX + 3, livroY + 5);
        g2d.drawLine(livroX - 3, livroY - 5, livroX - 3, livroY + 5);
        g2d.drawLine(livroX - 10, livroY - 2, livroX - 6, livroY - 2);
        g2d.drawLine(livroX + 6, livroY - 2, livroX + 10, livroY - 2);
        g2d.drawLine(livroX - 10, livroY + 2, livroX - 6, livroY + 2);
        g2d.drawLine(livroX + 6, livroY + 2, livroX + 10, livroY + 2);

        // Pincel (arte)
        int pinX = (int) (w * 0.78 + Math.sin(a * 0.7 + 2) * 5);
        int pinY = (int) (h * 0.28 + Math.cos(a * 0.8 + 2) * 5);
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.setColor(new Color(200, 180, 160, 160));
        g2d.drawLine(pinX, pinY + 8, pinX, pinY - 6);
        g2d.setColor(new Color(255, 180, 100, 180));
        g2d.fillOval(pinX - 3, pinY - 8, 7, 7);
        g2d.setColor(new Color(255, 200, 150, 140));
        g2d.drawOval(pinX - 3, pinY - 8, 7, 7);
        g2d.setStroke(s);

        // Haltere (fitness)
        int halX = (int) (w * 0.22 + Math.sin(a * 0.6 + 3) * 5);
        int halY = (int) (h * 0.30 + Math.cos(a * 0.5 + 3) * 5);
        g2d.setColor(new Color(150, 150, 170, 160));
        int barW = 20, barH = 4, pesoW = 6, pesoH = 9;
        g2d.fillRoundRect(halX - barW/2, halY - barH/2, barW, barH, 2, 2);
        g2d.fillRoundRect(halX - barW/2 - pesoW + 1, halY - pesoH/2, pesoW, pesoH, 2, 2);
        g2d.fillRoundRect(halX + barW/2 - 1, halY - pesoH/2, pesoW, pesoH, 2, 2);
        g2d.setColor(new Color(120, 120, 140, 180));
        g2d.drawRoundRect(halX - barW/2, halY - barH/2, barW, barH, 2, 2);
        g2d.drawRoundRect(halX - barW/2 - pesoW + 1, halY - pesoH/2, pesoW, pesoH, 2, 2);
        g2d.drawRoundRect(halX + barW/2 - 1, halY - pesoH/2, pesoW, pesoH, 2, 2);

        // Bolinhas decorativas pulsando com gradiente de cores
        for (int i = 0; i < 5; i++) {
            float by = (float) (h * 0.88 + Math.sin(a + i * 1.1) * 5);
            float bx = (float)(w * 0.25 + i * 25);
            float bs = 8 + (float)Math.sin(a * 1.5 + i) * 1.5f;
            Color bc;
            switch (i % 4) {
                case 0: bc = new Color(255, 200, 220, 130); break;
                case 1: bc = new Color(200, 230, 200, 130); break;
                case 2: bc = new Color(200, 220, 255, 130); break;
                default: bc = new Color(255, 240, 180, 130); break;
            }
            g2d.setColor(bc);
            g2d.fillOval((int)bx, (int)(by - bs/2), (int)bs, (int)bs);
        }
        for (int i = 0; i < 4; i++) {
            float by = (float) (h * 0.84 + Math.cos(a + i * 1.3) * 5);
            float bx = (float)(w * 0.62 + i * 25);
            float bs = 9 + (float)Math.cos(a * 1.3 + i * 0.7) * 2;
            Color bc;
            switch (i % 4) {
                case 0: bc = new Color(200, 230, 200, 120); break;
                case 1: bc = new Color(255, 200, 220, 120); break;
                case 2: bc = new Color(255, 240, 180, 120); break;
                default: bc = new Color(200, 220, 255, 120); break;
            }
            g2d.setColor(bc);
            g2d.fillOval((int)bx, (int)(by - bs/2), (int)bs, (int)bs);
        }
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

class MenuEmJogo extends JPanel implements ActionListener {
    private JogoAudrey frame;
    private JogoPanel jogoPanel;
    private JButton btnContinuar, btnSalvar, btnConfig, btnSobre, btnMenuPrincipal, btnSair;
    private Font fontCrayonHand, fontTitulo;
    private Color corPrincipal;
    private float animAngulo = 0;
    private Timer animTimer;

    public MenuEmJogo(JogoAudrey frame, JogoPanel jogoPanel) {
        this.frame = frame;
        this.jogoPanel = jogoPanel;
        corPrincipal = new Color(255, 230, 235); // Rosa pastel claro
        setBackground(new Color(255, 245, 235)); // Creme pastel
        setLayout(null);

        carregarFonts();
        criarComponentes();
        animTimer = new Timer(30, this);
        animTimer.start();
    }

    private void carregarFonts() {
        try {
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf"))
                    .deriveFont(24f);
            fontTitulo = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("KGSecondChancesSketch.ttf"))
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

        btnConfig = criarBotao("CONFIGURACOES");
        btnConfig.addActionListener(e -> frame.mostrarConfiguracoes("menuEmJogo"));
        add(btnConfig);

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
        int bh = Math.max(55, h / 12);
        int bx = (w - bw) / 2;
        int startY = (int) (h * 0.32);
        int gap = (int) (h * 0.10);
        JButton[] btns = { btnContinuar, btnSalvar, btnConfig, btnSobre, btnMenuPrincipal, btnSair };
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
    public void actionPerformed(ActionEvent e) {
        animAngulo += 0.04f;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int W = getWidth(), H = getHeight();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(255, 245, 235),
                W, H, new Color(255, 230, 240)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, W, H);

        g2d.setColor(new Color(200, 240, 210, 40));
        g2d.fillOval(-80, -80, W/2, H/2);
        g2d.setColor(new Color(255, 250, 200, 35));
        g2d.fillOval(W*2/3, H/3, W/2, H/2);
        g2d.setColor(new Color(200, 220, 255, 30));
        g2d.fillOval(W/3, H*2/3, W/2, H/3);
        g2d.setColor(new Color(230, 210, 255, 30));
        g2d.fillOval(W/4, H/4, W/3, H/3);

        desenharCantosDecorativos(g2d, W, H);
        desenharParticulasFlutuantes(g2d, W, H);
        desenharIconesHobbiesMenuPausa(g2d, W, H);

        // Banner decorativo atrás do título PAUSADO
        g2d.setFont(fontTitulo);
        String titulo = "PAUSADO";
        FontMetrics fm = g2d.getFontMetrics();
        int tx = (W - fm.stringWidth(titulo)) / 2;
        int titleY = (int) (H * 0.22);
        int bannerW = fm.stringWidth(titulo) + 100;
        int bannerH = fm.getHeight() + 40;
        int bannerX = (W - bannerW) / 2;
        int bannerY = titleY - fm.getAscent() - 15;

        g2d.setColor(new Color(180, 200, 180, 50));
        g2d.fillRoundRect(bannerX + 5, bannerY + 5, bannerW, bannerH, 35, 35);
        GradientPaint bannerGrad = new GradientPaint(bannerX, bannerY, new Color(255, 255, 245, 240), bannerX, bannerY + bannerH, new Color(240, 255, 245, 240));
        g2d.setPaint(bannerGrad);
        g2d.fillRoundRect(bannerX, bannerY, bannerW, bannerH, 35, 35);
        g2d.setColor(new Color(180, 230, 180, 160));
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawRoundRect(bannerX, bannerY, bannerW, bannerH, 35, 35);

        // Glow externo do título
        g2d.setFont(fontTitulo.deriveFont(Font.BOLD, 62f));
        fm = g2d.getFontMetrics();
        tx = (W - fm.stringWidth(titulo)) / 2;

        g2d.setColor(new Color(180, 220, 180, 40));
        g2d.drawString(titulo, tx + 8, titleY + 8);
        g2d.drawString(titulo, tx - 8, titleY - 8);
        g2d.setColor(new Color(255, 200, 150, 50));
        g2d.drawString(titulo, tx + 6, titleY - 6);
        g2d.drawString(titulo, tx - 6, titleY + 6);

        g2d.setColor(new Color(180, 210, 180, 70));
        g2d.drawString(titulo, tx + 5, titleY + 5);
        g2d.drawString(titulo, tx - 5, titleY - 5);

        g2d.setColor(new Color(200, 160, 120, 150));
        g2d.drawString(titulo, tx + 3, titleY + 3);

        GradientPaint titleGrad = new GradientPaint(tx, titleY - 60, new Color(255, 180, 100), tx, titleY + 10,
                new Color(100, 200, 160));
        g2d.setPaint(titleGrad);
        g2d.drawString(titulo, tx, titleY);

        // Estrelas decorativas ao lado do título
        float a = animAngulo;
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int starR = 7;
        int esx = tx - 30 + (int)(Math.sin(a * 0.7) * 3);
        int esy = titleY - fm.getAscent()/2 + (int)(Math.cos(a * 0.8) * 3);
        g2d.setColor(new Color(255, 215, 100, 200));
        g2d.drawLine(esx - starR, esy, esx + starR, esy);
        g2d.drawLine(esx, esy - starR, esx, esy + starR);

        int edx = tx + fm.stringWidth(titulo) + 30 + (int)(Math.sin(a * 0.9 + 1) * 3);
        int edy = titleY - fm.getAscent()/2 + (int)(Math.cos(a * 1.0 + 1) * 3);
        g2d.setColor(new Color(180, 230, 180, 200));
        g2d.drawLine(edx - starR, edy, edx + starR, edy);
        g2d.drawLine(edx, edy - starR, edx, edy + starR);

        // Painel translúcido atrás dos botões
        int btnStartY = (int) (H * 0.32);
        int btnEndY = btnStartY + 6 * (int) (H * 0.10) + 20;
        int cardW = Math.max(320, W / 3);
        int cardX = (W - cardW) / 2;
        int cardH = btnEndY - btnStartY + 40;

        g2d.setColor(new Color(255, 255, 245, 90));
        g2d.fillRoundRect(cardX, btnStartY - 20, cardW, cardH, 35, 35);
        g2d.setColor(new Color(180, 230, 180, 60));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(cardX, btnStartY - 20, cardW, cardH, 35, 35);
    }

    private void desenharCantosDecorativos(Graphics2D g2d, int w, int h) {
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int marg = 22, tam = 32;
        Color[] cores = {
            new Color(180, 230, 180, 140),
            new Color(255, 215, 100, 140),
            new Color(255, 180, 200, 140),
            new Color(180, 210, 255, 140)
        };

        g2d.setColor(cores[0]);
        g2d.drawArc(marg, marg, tam*2, tam*2, 180, 90);
        g2d.fillOval(marg - 3, marg - 3, 6, 6);
        g2d.fillOval(marg + tam - 3, marg + tam - 3, 6, 6);

        g2d.setColor(cores[1]);
        g2d.drawArc(w - marg - tam*2, marg, tam*2, tam*2, 270, 90);
        g2d.fillOval(w - marg - 3, marg - 3, 6, 6);
        g2d.fillOval(w - marg - tam - 3, marg + tam - 3, 6, 6);

        g2d.setColor(cores[2]);
        g2d.drawArc(marg, h - marg - tam*2, tam*2, tam*2, 90, 90);
        g2d.fillOval(marg - 3, h - marg - 3, 6, 6);
        g2d.fillOval(marg + tam - 3, h - marg - tam - 3, 6, 6);

        g2d.setColor(cores[3]);
        g2d.drawArc(w - marg - tam*2, h - marg - tam*2, tam*2, tam*2, 0, 90);
        g2d.fillOval(w - marg - 3, h - marg - 3, 6, 6);
        g2d.fillOval(w - marg - tam - 3, h - marg - tam - 3, 6, 6);
    }

    private void desenharParticulasFlutuantes(Graphics2D g2d, int w, int h) {
        float a = animAngulo;
        for (int i = 0; i < 30; i++) {
            float cx = (float) ((Math.sin(i * 3.7 + a * 0.25 + i * 0.3) * 0.5 + 0.5) * w);
            float cy = (float) ((Math.cos(i * 2.1 + a * 0.18 + i * 0.5) * 0.5 + 0.5) * h);
            int sz = (i % 4 == 0) ? 6 : (i % 4 == 1) ? 4 : 3;
            int alpha = 40 + (i % 6) * 25;
            Color cor;
            switch (i % 6) {
                case 0: cor = new Color(255, 200, 220, Math.min(alpha, 160)); break;
                case 1: cor = new Color(200, 235, 200, Math.min(alpha, 140)); break;
                case 2: cor = new Color(200, 215, 255, Math.min(alpha, 150)); break;
                case 3: cor = new Color(255, 240, 180, Math.min(alpha, 150)); break;
                case 4: cor = new Color(235, 210, 255, Math.min(alpha, 140)); break;
                default: cor = new Color(255, 220, 200, Math.min(alpha, 130)); break;
            }

            int tipo = i % 5;
            if (tipo == 0) {
                int r = sz / 2 + 1;
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.setColor(cor);
                g2d.drawLine((int)(cx - r), (int)cy, (int)(cx + r), (int)cy);
                g2d.drawLine((int)cx, (int)(cy - r), (int)cx, (int)(cy + r));
                g2d.drawLine((int)(cx - r/2), (int)(cy - r/2), (int)(cx + r/2), (int)(cy + r/2));
                g2d.drawLine((int)(cx - r/2), (int)(cy + r/2), (int)(cx + r/2), (int)(cy - r/2));
            } else if (tipo == 1) {
                int hsz = sz;
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.setColor(cor);
                GeneralPath hp = new GeneralPath();
                hp.moveTo(cx, cy + hsz/3);
                hp.curveTo(cx, cy - hsz/3, cx - hsz, cy - hsz/3, cx - hsz, cy);
                hp.curveTo(cx - hsz, cy + hsz*0.5, cx, cy + hsz, cx, cy + hsz/3);
                hp.curveTo(cx, cy + hsz, cx + hsz, cy + hsz*0.5, cx + hsz, cy);
                hp.curveTo(cx + hsz, cy - hsz/3, cx, cy - hsz/3, cx, cy + hsz/3);
                g2d.draw(hp);
            } else {
                g2d.setColor(cor);
                g2d.fillOval((int)cx, (int)cy, sz, sz);
            }
        }
    }

    private void desenharIconesHobbiesMenuPausa(Graphics2D g2d, int w, int h) {
        float a = animAngulo;
        g2d.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Borboleta (centro)
        int borX = (int) (w * 0.50 + Math.sin(a * 0.5 + 1) * 6);
        int borY = (int) (h * 0.15 + Math.cos(a * 0.6 + 1) * 5);
        float asa = 1.0f + (float) Math.sin(a * 2) * 0.2f;
        g2d.setColor(new Color(255, 200, 220, 140));
        int asaW = (int) (12 * asa);
        int asaH = 8;
        g2d.drawOval(borX - asaW, borY - asaH, asaW, asaH);
        g2d.drawOval(borX, borY - asaH, asaW, asaH);
        g2d.drawOval(borX - asaW/2, borY, asaW/2, asaH/2);
        g2d.drawOval(borX + asaW/4, borY, asaW/2, asaH/2);
        g2d.setColor(new Color(255, 180, 200, 160));
        g2d.fillOval(borX - 2, borY - 2, 4, 4);
        g2d.drawLine(borX - 2, borY - 3, borX - 5, borY - 9);
        g2d.drawLine(borX + 2, borY - 3, borX + 5, borY - 9);

        // Livro (leitura)
        int livroX = (int) (w * 0.15 + Math.sin(a * 0.5 + 2) * 5);
        int livroY = (int) (h * 0.75 + Math.cos(a * 0.6 + 2) * 4);
        g2d.setColor(new Color(200, 170, 140, 150));
        g2d.fillRoundRect(livroX - 12, livroY - 9, 24, 18, 5, 5);
        g2d.setColor(new Color(160, 130, 100, 170));
        g2d.drawRoundRect(livroX - 12, livroY - 9, 24, 18, 5, 5);
        g2d.drawLine(livroX, livroY - 7, livroX, livroY + 7);
        g2d.drawLine(livroX + 2, livroY - 5, livroX + 2, livroY + 5);
        g2d.drawLine(livroX - 2, livroY - 5, livroX - 2, livroY + 5);
        g2d.drawLine(livroX - 8, livroY - 2, livroX - 4, livroY - 2);
        g2d.drawLine(livroX + 4, livroY - 2, livroX + 8, livroY - 2);

        // Paleta (arte)
        int palX = (int) (w * 0.82 + Math.sin(a * 0.7 + 3) * 5);
        int palY = (int) (h * 0.25 + Math.cos(a * 0.8 + 3) * 4);
        g2d.setColor(new Color(220, 200, 180, 140));
        g2d.fillOval(palX - 10, palY - 7, 22, 16);
        g2d.setColor(new Color(180, 150, 130, 160));
        g2d.drawOval(palX - 10, palY - 7, 22, 16);
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(255, 100, 100, 170));
        g2d.fillOval(palX - 6, palY - 4, 4, 4);
        g2d.setColor(new Color(100, 200, 100, 170));
        g2d.fillOval(palX + 2, palY - 4, 4, 4);
        g2d.setColor(new Color(100, 150, 255, 170));
        g2d.fillOval(palX + 6, palY, 4, 4);
        g2d.setColor(new Color(255, 200, 50, 170));
        g2d.fillOval(palX - 2, palY + 2, 4, 4);
        g2d.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Haltere (fitness)
        int halX = (int) (w * 0.80 + Math.sin(a * 0.6 + 4) * 5);
        int halY = (int) (h * 0.75 + Math.cos(a * 0.5 + 4) * 4);
        g2d.setColor(new Color(150, 150, 170, 150));
        int barW = 18, barH = 4, pesoW = 5, pesoH = 8;
        g2d.fillRoundRect(halX - barW/2, halY - barH/2, barW, barH, 2, 2);
        g2d.fillRoundRect(halX - barW/2 - pesoW + 1, halY - pesoH/2, pesoW, pesoH, 2, 2);
        g2d.fillRoundRect(halX + barW/2 - 1, halY - pesoH/2, pesoW, pesoH, 2, 2);
        g2d.setColor(new Color(120, 120, 140, 170));
        g2d.drawRoundRect(halX - barW/2, halY - barH/2, barW, barH, 2, 2);
        g2d.drawRoundRect(halX - barW/2 - pesoW + 1, halY - pesoH/2, pesoW, pesoH, 2, 2);
        g2d.drawRoundRect(halX + barW/2 - 1, halY - pesoH/2, pesoW, pesoH, 2, 2);

        // Bolinhas decorativas
        for (int i = 0; i < 4; i++) {
            float by = (float) (h * 0.88 + Math.sin(a + i * 1.1) * 4);
            float bx = (float)(w * 0.20 + i * 22);
            float bs = 8 + (float)Math.sin(a * 1.4 + i) * 1.5f;
            Color bc;
            switch (i % 4) {
                case 0: bc = new Color(255, 200, 220, 120); break;
                case 1: bc = new Color(200, 230, 200, 120); break;
                case 2: bc = new Color(200, 220, 255, 120); break;
                default: bc = new Color(255, 240, 180, 120); break;
            }
            g2d.setColor(bc);
            g2d.fillOval((int)bx, (int)(by - bs/2), (int)bs, (int)bs);
        }
    }
}

class JogoPanel extends JPanel implements ActionListener, KeyListener, MouseListener {
    private JogoAudrey frame;
    private final int LARGURA = 1000;
    private final int ALTURA = 750;

    private Image fundoCenario1, fundoCenario2, fundoCenario3, fundoGinasio, fundoSalaAula1, fundoBiblioteca, imgArmarioAberto, imgNicolas;
    private Image imgChave, imgLivro;
    private Image imgAlunoCorredor1, imgAlunoCorredor2;
    private Image[] framesAndar = new Image[2];
    private Image imgParada;
    private Image imgPortraitAudrey, imgPortraitNicollas, imgPortraitGabi, imgPortraitIvi;

    private final int AUDREY_LARGURA = 240;
    private final int AUDREY_ALTURA = 430;
    private final int NPC_LARGURA = 400;
    private final int NPC_ALTURA = 450;
    private int audreyX = 100, audreyY = 730, velX = 0;
    private int frameAtual = 0, contadorAnimacao = 0;

    private final int NICOLAS_LARGURA = 350;
    private final int NICOLAS_ALTURA = 520;

    private boolean olhandoDireita = true, estaMovendo = false;
    private boolean temChave = false, armarioEstaAberto = false;
    private boolean estaEmDialogoNicolas = false;
    private boolean nicolasJaFoiEncontrado = false;
    private String[] dialogoNicolasIntro = {
            "Oi! Você deve ser a aluna nova, né? Eu sou o Nicollas, muito prazer!",
            "Que bom te conhecer, Audrey! Eu ajudo os alunos com as missões por aqui.",
            "Qualquer dúvida que tiver, pode contar comigo, tá?",
            "Vamos começar sua aventura juntos! Vai ser incrível!"
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

    // --- EPISODIO 1 ---
    private boolean ep1FalouNpc1 = false;
    private boolean ep1FalouNpc2 = false;
    private boolean ep1FalouNpc3 = false;
    private int faseDialogoGabi = 0;
    private int faseDialogoIvi = 0;
    private boolean ep1InteragiuBiblioteca = false;
    private boolean ep1InteragiuMural = false;
    private boolean sala1Aberta = false;
    private int faseDialogoEp1 = 0;
    private boolean missaoDesenhoAtiva = false;
    private boolean desenhoEntregue = false;
    private boolean episodio1Concluido = false;
    
    private Image imgCamila;

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
    private BufferedImage shaderBuffer = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_ARGB);

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

        props.setProperty("ep1FalouNpc1", String.valueOf(ep1FalouNpc1));
        props.setProperty("ep1FalouNpc2", String.valueOf(ep1FalouNpc2));
        props.setProperty("ep1FalouNpc3", String.valueOf(ep1FalouNpc3));
        props.setProperty("faseDialogoGabi", String.valueOf(faseDialogoGabi));
        props.setProperty("faseDialogoIvi", String.valueOf(faseDialogoIvi));
        props.setProperty("ep1InteragiuBiblioteca", String.valueOf(ep1InteragiuBiblioteca));
        props.setProperty("ep1InteragiuMural", String.valueOf(ep1InteragiuMural));
        props.setProperty("sala1Aberta", String.valueOf(sala1Aberta));
        props.setProperty("faseDialogoEp1", String.valueOf(faseDialogoEp1));
        props.setProperty("missaoDesenhoAtiva", String.valueOf(missaoDesenhoAtiva));
        props.setProperty("desenhoEntregue", String.valueOf(desenhoEntregue));
        props.setProperty("episodio1Concluido", String.valueOf(episodio1Concluido));

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

        ep1FalouNpc1 = Boolean.parseBoolean(props.getProperty("ep1FalouNpc1", "false"));
        ep1FalouNpc2 = Boolean.parseBoolean(props.getProperty("ep1FalouNpc2", "false"));
        ep1FalouNpc3 = Boolean.parseBoolean(props.getProperty("ep1FalouNpc3", "false"));
        faseDialogoGabi = Integer.parseInt(props.getProperty("faseDialogoGabi", "0"));
        faseDialogoIvi = Integer.parseInt(props.getProperty("faseDialogoIvi", "0"));
        ep1InteragiuBiblioteca = Boolean.parseBoolean(props.getProperty("ep1InteragiuBiblioteca", "false"));
        ep1InteragiuMural = Boolean.parseBoolean(props.getProperty("ep1InteragiuMural", "false"));
        sala1Aberta = Boolean.parseBoolean(props.getProperty("sala1Aberta", "false"));
        faseDialogoEp1 = Integer.parseInt(props.getProperty("faseDialogoEp1", "0"));
        missaoDesenhoAtiva = Boolean.parseBoolean(props.getProperty("missaoDesenhoAtiva", "false"));
        desenhoEntregue = Boolean.parseBoolean(props.getProperty("desenhoEntregue", "false"));
        episodio1Concluido = Boolean.parseBoolean(props.getProperty("episodio1Concluido", "false"));

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
        fundoCenario1 = redimensionarFundo(new ImageIcon("corredor1.png").getImage(), LARGURA, ALTURA);
        fundoCenario2 = redimensionarFundo(new ImageIcon("corredor2_novo.jpg").getImage(), LARGURA, ALTURA);
        fundoCenario3 = redimensionarFundo(new ImageIcon("ChatGPT Image 18 de jun. de 2026, 09_18_24.png").getImage(), LARGURA, ALTURA);
        fundoGinasio = redimensionarFundo(new ImageIcon("ginasio.png").getImage(), LARGURA, ALTURA);
        fundoBiblioteca = redimensionarFundo(new ImageIcon("bibliotecasala4.png").getImage(), LARGURA, ALTURA);
        fundoSalaAula1 = redimensionarFundo(new ImageIcon("saladeaula1.png").getImage(), LARGURA, ALTURA);
        imgArmarioAberto = new ImageIcon("imagemarmario.png").getImage();
        imgNicolas = null;
        imgChave = new ImageIcon("chave.png").getImage();
        imgLivro = new ImageIcon("livro.png").getImage();

        imgAlunoCorredor1 = redimensionarImagem(new ImageIcon("aluno_corredor1.png").getImage(), NPC_LARGURA, NPC_ALTURA);
        imgAlunoCorredor2 = redimensionarImagem(new ImageIcon("aluno_corredor2.png").getImage(), NPC_LARGURA, NPC_ALTURA);
        imgPortraitAudrey = carregarImagem("portrait_audrey-removebg-preview.png");
        imgPortraitNicollas = null;
        imgPortraitGabi = carregarImagem("gabi_personagem-removebg-preview.png");
        imgPortraitIvi = carregarImagem("1000115243-removebg-preview.png");
        imgCamila = null;

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

    private Image redimensionarFundo(Image img, int targetWidth, int targetHeight) {
        return img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        boolean usarShader = Configuracoes.getInstance().isShaderAtivo();
        Graphics2D g2dReal = (Graphics2D) g;
        Graphics2D g2d = usarShader ? shaderBuffer.createGraphics() : g2dReal;

        if (usarShader) {
            // Limpa o buffer transparente
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, LARGURA, ALTURA);
            g2d.setComposite(AlphaComposite.SrcOver);
        }

        int w = getWidth();
        int h = getHeight();

        // Calcular a escala preservando a proporção de 1000x750
        double scaleX = (double) w / LARGURA;
        double scaleY = (double) h / ALTURA;
        double scale = Math.min(scaleX, scaleY);

        int xOffset = (int) ((w - (LARGURA * scale)) / 2);
        int yOffset = (int) ((h - (ALTURA * scale)) / 2);

        if (!usarShader) {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, w, h);
            g2d.translate(xOffset, yOffset);
            g2d.scale(scale, scale);
            g2d.setClip(0, 0, LARGURA, ALTURA);
        }

        if (!armarioEstaAberto) {
            if (indiceMapa == 0) {
                g2d.drawImage(fundoCenario1, 0, 0, LARGURA, ALTURA, this);
            } else if (indiceMapa == 1) {
                g2d.drawImage(fundoCenario2, 0, 0, LARGURA, ALTURA, this);
            } else if (indiceMapa == 2) {
                g2d.drawImage(fundoSalaAula1, 0, 0, LARGURA, ALTURA, this);
            } else if (indiceMapa == 3) {
                g2d.drawImage(fundoCenario3, 0, 0, LARGURA, ALTURA, this);
            } else if (indiceMapa == 4) {
                g2d.drawImage(fundoGinasio, 0, 0, LARGURA, ALTURA, this);
            } else if (indiceMapa == 5) {
                g2d.drawImage(fundoCenario1, 0, 0, LARGURA, ALTURA, this);
            } else if (indiceMapa == 6) {
                g2d.drawImage(fundoBiblioteca, 0, 0, LARGURA, ALTURA, this);
            }


            // Desenhar NPCs do corredor
            if (indiceMapa == 1) {
                if (imgAlunoCorredor1 != null) {
                    g2d.drawImage(imgAlunoCorredor1, 550, audreyY - NPC_ALTURA, NPC_LARGURA, NPC_ALTURA, this);
                }
                if (imgAlunoCorredor2 != null) {
                    g2d.drawImage(imgAlunoCorredor2, 750, audreyY - NPC_ALTURA, NPC_LARGURA, NPC_ALTURA, this);
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

        int brilhoVal = Configuracoes.getInstance().getBrilho();
        if (brilhoVal != 50) {
            if (brilhoVal < 50) {
                int alphaB = (int) ((50 - brilhoVal) / 50.0 * 180);
                g2d.setColor(new Color(0, 0, 0, Math.min(alphaB, 255)));
            } else {
                int alphaB = (int) ((brilhoVal - 50) / 50.0 * 100);
                g2d.setColor(new Color(255, 255, 255, Math.min(alphaB, 255)));
            }
            g2d.fillRect(0, 0, LARGURA, ALTURA);
        }

        if (usarShader) {
            g2d.dispose(); // Libera o graphics do buffer
            PastelShader.aplicarFiltro(shaderBuffer);

            // Agora desenha na tela real com as devidas proporções/bordas
            g2dReal.setColor(Color.BLACK);
            g2dReal.fillRect(0, 0, w, h);
            g2dReal.translate(xOffset, yOffset);
            g2dReal.scale(scale, scale);
            g2dReal.setClip(0, 0, LARGURA, ALTURA);
            g2dReal.drawImage(shaderBuffer, 0, 0, null);
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

            String teclaE = Configuracoes.getInstance().getNomeTecla("INTERAGIR");
            String teclaF = Configuracoes.getInstance().getNomeTecla("FALAR");

            if (indiceMapa == 0 && Math.abs(audreyX - posArmarioX) < 150) {
                desenharTextoComSombra(g2d, "Pressione [" + teclaE + "] para abrir", audreyX - 10, audreyY - 430, Color.CYAN);
            }

            if (indiceMapa == 1) {
                if (Math.abs(audreyX - 550) < 150) {
                    desenharTextoComSombra(g2d, "Pressione [" + teclaF + "] para falar com a Ivi", audreyX - 10, audreyY - 430,
                            Color.YELLOW);
                } else if (Math.abs(audreyX - 750) < 150) {
                    desenharTextoComSombra(g2d, "Pressione [" + teclaF + "] para falar com a Gabi", audreyX - 10, audreyY - 430,
                            Color.YELLOW);
                } else if (Math.abs(audreyX - posPortaX) < 150) {
                    if (explorouCorredor) {
                        desenharTextoComSombra(g2d, "Pressione [" + teclaE + "] para entrar", audreyX - 10, audreyY - 430, Color.GREEN);
                    } else {
                        desenharTextoComSombra(g2d, "Vou explorar o final do corredor primeiro...", audreyX - 10,
                                audreyY - 430, Color.LIGHT_GRAY);
                    }
                }
            }

            if (indiceMapa == 2 && Math.abs(audreyX - posNicolasXSalaAula) < 120) {
                desenharTextoComSombra(g2d, "Pressione [" + teclaF + "] para falar", audreyX - 10, audreyY - 430, Color.YELLOW);
            }

            if (indiceMapa == 2 && estaProximoDaPuerta()) {
                desenharTextoComSombra(g2d, "Pressione [" + teclaE + "] para sair", audreyX - 10, audreyY - 430, Color.RED);
            }

            if (indiceMapa == 3) {
                if (Math.abs(audreyX - 350) < 150) {
                    desenharTextoComSombra(g2d, "Pressione [" + teclaE + "] para abrir", audreyX - 10, audreyY - 430, Color.LIGHT_GRAY);
                } else if (Math.abs(audreyX - 750) < 150) {
                    desenharTextoComSombra(g2d, "Pressione [" + teclaE + "] para entrar na biblioteca", audreyX - 10, audreyY - 430, Color.GREEN);
                }
            }
            
            if (indiceMapa == 6 && Math.abs(audreyX - 50) < 150) {
                desenharTextoComSombra(g2d, "Pressione [" + teclaE + "] para sair da biblioteca", audreyX - 10, audreyY - 430, Color.RED);
            }
        }

        if (!textoDialogo.isEmpty() || armarioEstaAberto) {
            int caixaH = 120;
            int caixaY = ALTURA - caixaH - 20;

            g2d.setColor(new Color(253, 246, 227, 230));
            g2d.fillRoundRect(50, caixaY, 900, caixaH, 20, 20);

            g2d.setColor(new Color(210, 180, 140));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(50, caixaY, 900, caixaH, 20, 20);

            int textXOffset = 80;

            if ("Audrey".equals(nomePersonagem) && imgPortraitAudrey != null) {
                int imgW = imgPortraitAudrey.getWidth(this);
                int imgH = imgPortraitAudrey.getHeight(this);
                int drawH = 200;
                int drawW = (imgH > 0) ? (int) (imgW * ((double) drawH / imgH)) : 150;

                g2d.drawImage(imgPortraitAudrey, 70, caixaY + (caixaH - drawH) / 2 - 15, drawW, drawH, this);
                textXOffset = 70 + drawW + 20;
            } else if ("Nicollas".equals(nomePersonagem) && imgPortraitNicollas != null) {
                int imgW = imgPortraitNicollas.getWidth(this);
                int imgH = imgPortraitNicollas.getHeight(this);
                int drawH = 200;
                int drawW = (imgH > 0) ? (int) (imgW * ((double) drawH / imgH)) : 150;

                g2d.drawImage(imgPortraitNicollas, 70, caixaY + (caixaH - drawH) / 2, drawW, drawH, this);
                textXOffset = 70 + drawW + 20;
            } else if ("Gabi".equals(nomePersonagem) && imgPortraitGabi != null) {
                int imgW = imgPortraitGabi.getWidth(this);
                int imgH = imgPortraitGabi.getHeight(this);
                int drawH = 200;
                int drawW = (imgH > 0) ? (int) (imgW * ((double) drawH / imgH)) : 150;

                Shape oldClip = g2d.getClip();
                g2d.setClip(new RoundRectangle2D.Float(70, caixaY + (caixaH - drawH) / 2, drawW, drawH, 30, 30));
                g2d.drawImage(imgPortraitGabi, 70, caixaY + (caixaH - drawH) / 2, drawW, drawH, this);
                g2d.setClip(oldClip);
                textXOffset = 70 + drawW + 20;
            } else if ("Ivi".equals(nomePersonagem) && imgPortraitIvi != null) {
                int imgW = imgPortraitIvi.getWidth(this);
                int imgH = imgPortraitIvi.getHeight(this);
                int drawH = 200;
                int drawW = (imgH > 0) ? (int) (imgW * ((double) drawH / imgH)) : 150;

                Shape oldClip = g2d.getClip();
                g2d.setClip(new RoundRectangle2D.Float(70, caixaY + (caixaH - drawH) / 2, drawW, drawH, 30, 30));
                g2d.drawImage(imgPortraitIvi, 70, caixaY + (caixaH - drawH) / 2, drawW, drawH, this);
                g2d.setClip(oldClip);
                textXOffset = 70 + drawW + 20;
            }

            if (!nomePersonagem.isEmpty() && !armarioEstaAberto) {
                g2d.setFont(fontCrayonHand.deriveFont(Font.BOLD, 16));
                g2d.setColor(new Color(160, 82, 45));
                g2d.drawString(nomePersonagem, textXOffset, caixaY + 25);
            }

            g2d.setFont(fontCrayonHand.deriveFont(Font.BOLD, 18));
            String msg = armarioEstaAberto ? "Pressione 'Q' para fechar o armário." : textoDialogo;
            g2d.setColor(new Color(60, 50, 40));
            int maxWidth = 900 - (textXOffset - 50) - 20;
            desenharTextoQuebrado(g2d, msg, textXOffset, caixaY + 50, maxWidth);

            if (!armarioEstaAberto && !textoDialogo.isEmpty()) {
                g2d.setFont(fontCrayonHand.deriveFont(Font.PLAIN, 13));
                g2d.setColor(new Color(140, 120, 100));

                if (estaEmDialogoNicolas && faseDialogoNicolas < 5) {
                    g2d.drawString("(Pressione [F] para próxima fala ou [Q] para fechar)", textXOffset, caixaY + 105);
                } else {
                    g2d.drawString("(Pressione [Q] para fechar)", textXOffset, caixaY + 105);
                }
            }
        }
    }

    private void desenharTextoQuebrado(Graphics2D g2d, String texto, int x, int y, int maxWidth) {
        FontMetrics fm = g2d.getFontMetrics();
        String[] palavras = texto.split(" ");
        String linhaAtual = "";
        int linhaY = y;

        for (String palavra : palavras) {
            if (fm.stringWidth(linhaAtual + palavra) < maxWidth) {
                linhaAtual += palavra + " ";
            } else {
                g2d.drawString(linhaAtual, x, linhaY);
                linhaAtual = palavra + " ";
                linhaY += fm.getHeight();
            }
        }
        g2d.drawString(linhaAtual, x, linhaY);
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

    private void checarObjetivosEp1() {
        if (!sala1Aberta && ep1FalouNpc2 && ep1FalouNpc3) {
            sala1Aberta = true;
            GerenciadorAudio.tocarSomSinalEscolar();
            estaEmDialogoNicolas = true;
            nomePersonagem = "Sistema";
            textoDialogo = "Agora que você conheceu a Gabi e a Ivi, a porta da Sala 1 está aberta!";
        }
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
                    indiceMapa = 3;
                    audreyX = 0;
                    textoDialogo = "";
                    nomePersonagem = "";
                    estaEmDialogoNicolas = false;
                }
            } else if (indiceMapa == 3) {
                if (audreyX < 0) {
                    audreyX = 0;
                    if (velX < 0) {
                        indiceMapa = 1;
                        audreyX = LARGURA - AUDREY_LARGURA;
                        textoDialogo = "";
                        nomePersonagem = "";
                    }
                }
                if (audreyX + AUDREY_LARGURA > LARGURA) {
                    indiceMapa = 4;
                    audreyX = 0;
                    textoDialogo = "";
                    nomePersonagem = "";
                }
            } else if (indiceMapa == 4) {
                if (audreyX < 0) {
                    audreyX = 0;
                    if (velX < 0) {
                        indiceMapa = 3;
                        audreyX = LARGURA - AUDREY_LARGURA;
                        textoDialogo = "";
                        nomePersonagem = "";
                    }
                }
                if (audreyX + AUDREY_LARGURA > LARGURA) {
                    indiceMapa = 5;
                    audreyX = 0;
                    textoDialogo = "";
                    nomePersonagem = "";
                }
            } else if (indiceMapa == 5) {
                if (audreyX < 0) {
                    audreyX = 0;
                    if (velX < 0) {
                        indiceMapa = 4;
                        audreyX = LARGURA - AUDREY_LARGURA;
                        textoDialogo = "";
                        nomePersonagem = "";
                    }
                }
                if (audreyX + AUDREY_LARGURA > LARGURA) {
                    audreyX = LARGURA - AUDREY_LARGURA;
                }
            } else if (indiceMapa == 2) {
                if (!cutsceneSalaVista) {
                    cutsceneSalaVista = true;
                    if (!episodio1Concluido && sala1Aberta) {
                        estaEmDialogoNicolas = true;
                        if (faseDialogoEp1 == 0) {
                            GerenciadorAudio.tocarSomDialogo();
                            nomePersonagem = "Audrey";
                            textoDialogo = "Oi, com licença... Sou a Audrey, aluna nova.";
                            faseDialogoEp1 = 1;
                        }
                    }
                }

                if (audreyX < 0) {
                    audreyX = 0;
                }
                if (audreyX + AUDREY_LARGURA > LARGURA) {
                    audreyX = LARGURA - AUDREY_LARGURA;
                }
            } else if (indiceMapa == 6) {
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

            if (indiceMapa == 1 && Math.abs(audreyX - posPortaX) > 200 && Math.abs(audreyX - 750) > 200 && Math.abs(audreyX - 550) > 200) {
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

        if (code == Configuracoes.getInstance().getTecla("INVENTARIO")) {
            inventarioAberto = !inventarioAberto;
            return;
        }

        // M para mostrar/esconder objetivos
        if (code == Configuracoes.getInstance().getTecla("OBJETIVOS")) {
            mostrarObjetivos = !mostrarObjetivos;
            return;
        }

        if (code == Configuracoes.getInstance().getTecla("DIREITA")) {
            velX = 13;
            olhandoDireita = true;
            estaMovendo = true;
        }

        if (code == Configuracoes.getInstance().getTecla("ESQUERDA")) {
            velX = -13;
            olhandoDireita = false;
            estaMovendo = true;
        }

        if (code == Configuracoes.getInstance().getTecla("DIARIO")) {
            diarioAberto = !diarioAberto;
            return;
        }

        if (code == Configuracoes.getInstance().getTecla("FALAR")) {
            // NPC 1 no Corredor 1
            if (indiceMapa == 0 && Math.abs(audreyX - 600) < 100 && textoDialogo.isEmpty()) {
                if (!ep1FalouNpc1) {
                    GerenciadorAudio.tocarSomDialogo();
                    estaEmDialogoNicolas = true;
                    nomePersonagem = "Aluno do Pátio";
                    textoDialogo = "Oi! Você é nova? A escola é grande, não se perca.";
                    ep1FalouNpc1 = true;
                    checarObjetivosEp1();
                    return;
                }
            }

            // NPC Gabi no Corredor 2
            if (indiceMapa == 1 && Math.abs(audreyX - 750) < 150 && Math.abs(audreyX - 550) >= 150) {
                estaEmDialogoNicolas = true;
                if (!ep1FalouNpc2) {
                    if (faseDialogoGabi == 0) {
                        GerenciadorAudio.tocarSomDialogo();
                        nomePersonagem = "Gabi";
                        textoDialogo = "Oi! Você é a Audrey, né? Eu sou a Gabi! Muito prazer te conhecer!";
                        faseDialogoGabi = 1;
                    } else if (faseDialogoGabi == 1) {
                        nomePersonagem = "Audrey";
                        textoDialogo = "Oi, Gabi! Sou sim, muito prazer! Tô conhecendo a escola ainda.";
                        faseDialogoGabi = 2;
                    } else if (faseDialogoGabi == 2) {
                        nomePersonagem = "Gabi";
                        textoDialogo = "Nossa, que legal ter alguém novo na escola! Já conheceu todo mundo?";
                        faseDialogoGabi = 3;
                    } else if (faseDialogoGabi == 3) {
                        nomePersonagem = "Audrey";
                        textoDialogo = "Ainda não, tô explorando aos poucos.";
                        faseDialogoGabi = 4;
                    } else if (faseDialogoGabi == 4) {
                        nomePersonagem = "Gabi";
                        textoDialogo = "A sala do primeiro ano é ali na porta à direita. O pessoal já tá organizando o festival cultural, tá bem animado!";
                        faseDialogoGabi = 5;
                    } else if (faseDialogoGabi == 5) {
                        nomePersonagem = "Audrey";
                        textoDialogo = "Legal! Vou dar uma olhada lá, obrigada!";
                        faseDialogoGabi = 6;
                    } else if (faseDialogoGabi == 6) {
                        nomePersonagem = "Gabi";
                        textoDialogo = "Ah, e procura a Ivi também! Ela é super gente boa, vocês vão se dar bem!";
                        faseDialogoGabi = 7;
                    } else {
                        textoDialogo = "";
                        ep1FalouNpc2 = true;
                        estaEmDialogoNicolas = false;
                        checarObjetivosEp1();
                    }
                } else {
                    if (faseDialogoGabi < 7) faseDialogoGabi = 7;
                    nomePersonagem = "Gabi";
                    textoDialogo = "Fala com a Ivi também, ela é super gente boa!";
                }
                explorouCorredor = true;
                return;
            }

            // NPC Ivi no Corredor 2
            if (indiceMapa == 1 && Math.abs(audreyX - 550) < 150) {
                estaEmDialogoNicolas = true;
                if (!ep1FalouNpc3) {
                    if (faseDialogoIvi == 0) {
                        GerenciadorAudio.tocarSomDialogo();
                        if (ep1FalouNpc2) {
                            nomePersonagem = "Ivi";
                            textoDialogo = "Audrey! Finalmente te conheço! A Gabi já falou super bem de você, sou a Ivi, prazer!";
                        } else {
                            nomePersonagem = "Ivi";
                            textoDialogo = "Oi! Você é a Audrey, né? Sou a Ivi, muito prazer! Tudo bem?";
                        }
                        faseDialogoIvi = 1;
                    } else if (faseDialogoIvi == 1) {
                        nomePersonagem = "Audrey";
                        if (ep1FalouNpc2) {
                            textoDialogo = "Oi, Ivi! Que bom que a Gabi falou de mim! Tudo bem sim, e você?";
                        } else {
                            textoDialogo = "Oi, Ivi! Sou sim, muito prazer! Tudo bem sim, e você?";
                        }
                        faseDialogoIvi = 2;
                    } else if (faseDialogoIvi == 2) {
                        nomePersonagem = "Ivi";
                        textoDialogo = "Tudo ótimo! Tá gostando da escola até agora? O pessoal da Sala 1 é muito legal.";
                        faseDialogoIvi = 3;
                    } else if (faseDialogoIvi == 3) {
                        nomePersonagem = "Audrey";
                        textoDialogo = "Tô sim! O pessoal parece bem acolhedor.";
                        faseDialogoIvi = 4;
                    } else if (faseDialogoIvi == 4) {
                        nomePersonagem = "Ivi";
                        textoDialogo = "A Raquel, o Nicollas e a Camila já estão lá dentro. Entra lá e conhece o pessoal!";
                        faseDialogoIvi = 5;
                    } else if (faseDialogoIvi == 5) {
                        nomePersonagem = "Audrey";
                        textoDialogo = "Vou sim, obrigada!";
                        faseDialogoIvi = 6;
                    } else if (faseDialogoIvi == 6) {
                        nomePersonagem = "Ivi";
                        textoDialogo = "Qualquer coisa que precisar, pode contar comigo, viu? Bem-vinda de verdade!";
                        faseDialogoIvi = 7;
                    } else {
                        textoDialogo = "";
                        ep1FalouNpc3 = true;
                        estaEmDialogoNicolas = false;
                        checarObjetivosEp1();
                    }
                } else {
                    if (faseDialogoIvi < 7) faseDialogoIvi = 7;
                    nomePersonagem = "Ivi";
                    textoDialogo = "A sala de aula do seu ano fica na porta atrás de você!";
                }
                explorouCorredor = true;
                return;
            }

            // Cena 2 na Sala 1
            if (indiceMapa == 2 && !episodio1Concluido && sala1Aberta) {
                if (Math.abs(audreyX - posNicolasXSalaAula) < 300) {
                    estaEmDialogoNicolas = true;
                    if (faseDialogoEp1 == 0) {
                        GerenciadorAudio.tocarSomDialogo();
                        nomePersonagem = "Audrey";
                        textoDialogo = "Oi, com licença... Sou a Audrey, aluna nova.";
                        faseDialogoEp1 = 1;
                    } else if (faseDialogoEp1 == 1) {
                        nomePersonagem = "Raquel";
                        textoDialogo = "Ah, oi! Seja bem-vinda! Eu sou a Raquel. Estávamos justamente discutindo sobre o projeto do festival cultural.";
                        faseDialogoEp1 = 2;
                    } else if (faseDialogoEp1 == 2) {
                        nomePersonagem = "Nicollas";
                        textoDialogo = "Salve, Audrey! Sou o Nicollas. Cara, a Raquel quer ir pelo caminho mais clássico, mas eu acho que a gente devia meter uma parada mais urbana, tipo um grafite expressionista. Arte tem que ter impacto, sabe? Sentimento bruto!";
                        faseDialogoEp1 = 3;
                    } else if (faseDialogoEp1 == 3) {
                        nomePersonagem = "Camila";
                        textoDialogo = "Sou a Camila. E o Nicollas esquece que o expressionismo é justamente sobre distorcer a realidade para expressar as emoções. Não precisa ser só grafite. A arte ganha vida quando o conceito é forte. E você, Audrey? Curte alguma vertente?";
                        faseDialogoEp1 = 4;
                    } else if (faseDialogoEp1 == 4) {
                        nomePersonagem = "Audrey";
                        textoDialogo = "Eu gosto bastante de desenhar, na verdade. Para mim, a arte ajuda a colocar para fora coisas que as palavras não dão conta.";
                        faseDialogoEp1 = 5;
                    } else if (faseDialogoEp1 == 5) {
                        nomePersonagem = "Raquel";
                        textoDialogo = "Sério? Que incrível! Olha, a gente está precisando de braço e de ideias novas para o grupo. Que tal um teste rápido para ver o seu estilo?";
                        faseDialogoEp1 = 6;
                    } else if (faseDialogoEp1 == 6) {
                        nomePersonagem = "Nicollas";
                        textoDialogo = "Boa! Vamos ver o seu nível artístico. Quero ver como você interpreta o tema: 'O Sentimento da Solidão Urbana'. Pode ser algo tecnológico, um cenário vazio, ou só alguém na multidão.";
                        faseDialogoEp1 = 7;
                    } else if (faseDialogoEp1 == 7) {
                        nomePersonagem = "Camila";
                        textoDialogo = "Mostra para a gente do que você é capaz. Estamos bem curiosos!";
                        faseDialogoEp1 = 8;
                    } else if (faseDialogoEp1 == 8) {
                        nomePersonagem = "Sistema";
                        textoDialogo = "[Missão: Desenhe o tema 'O Sentimento da Solidão Urbana' no mundo real]";
                        missaoDesenhoAtiva = true;
                        faseDialogoEp1 = 9;
                    } else if (faseDialogoEp1 == 9) {
                        textoDialogo = "";
                        estaEmDialogoNicolas = false;
                        faseDialogoEp1 = 10;
                    } else if (faseDialogoEp1 == 10) {
                        if (audreyX < posNicolasXSalaAula - 50) {
                            nomePersonagem = "Audrey";
                            textoDialogo = "Pronto, pessoal. Terminei o esboço do tema que vocês pediram. O que acham?";
                            faseDialogoEp1 = 11;
                        } else {
                            nomePersonagem = "Nicollas e Camila";
                            String[] falasEspera = {
                                "Mal posso esperar para ver seu desenho!",
                                "Como está ficando o esboço?"
                            };
                            textoDialogo = falasEspera[(int)(Math.random() * falasEspera.length)];
                            return;
                        }
                    } else if (faseDialogoEp1 == 11) {
                        nomePersonagem = "Camila";
                        textoDialogo = "Uau... Olha o uso das sombras aqui. Você conseguiu captar exatamente a atmosfera de isolamento.";
                        faseDialogoEp1 = 12;
                    } else if (faseDialogoEp1 == 12) {
                        nomePersonagem = "Nicollas";
                        textoDialogo = "Ficou irado! Essa perspectiva deu um peso enorme para o desenho. Você tem muita técnica.";
                        faseDialogoEp1 = 13;
                    } else if (faseDialogoEp1 == 13) {
                        nomePersonagem = "Raquel";
                        textoDialogo = "Perfeito! Você passou no teste com folga, Audrey. Agora você é oficialmente a mente criativa do nosso grupo. Pronta para os próximos desafios?";
                        faseDialogoEp1 = 14;
                    } else if (faseDialogoEp1 == 14) {
                        textoDialogo = "";
                        episodio1Concluido = true;
                        desenhoEntregue = true;
                        missaoDesenhoAtiva = false;
                        estaEmDialogoNicolas = false;
                        JOptionPane.showMessageDialog(this, "Fim do Episódio 1! Progresso Salvo.");
                        int slot = frame.getSlotAtual();
                        if (slot != -1) salvarEstado(slot);
                    }
                    return;
                }
            }

            if (indiceMapa == 2) {
                // Checa npc Leitura (x=300)
                if (Math.abs(audreyX - 300) < 120) {
                    estaEmDialogoNicolas = true; // reaproveitando a flag de estar em dialogo
                    nomePersonagem = "Nicollas";
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
                    nomePersonagem = "Ivi";
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
                    if (!missaoFitnessAtiva && !missaoFitnessConcluida) {
                        if (faseDialogoFitness == 0) {
                            nomePersonagem = "Nicollas";
                            GerenciadorAudio.tocarSomDialogo();
                            textoDialogo = "E aí! Você deve ser a Audrey, a aluna nova, né? Eu sou o Nicollas, muito prazer!";
                            faseDialogoFitness = 1;
                        } else if (faseDialogoFitness == 1) {
                            nomePersonagem = "Audrey";
                            textoDialogo = "Oi, Nicollas! Prazer. Sou nova aqui sim. Falaram que você ajuda com as missões?";
                            faseDialogoFitness = 2;
                        } else if (faseDialogoFitness == 2) {
                            nomePersonagem = "Nicollas";
                            textoDialogo = "Isso aí! Preparada? A chave de ouro é a disciplina. Toma aqui esse cronograma de treinos.";
                            temCronograma = true;
                            faseDialogoFitness = 3;
                        } else if (faseDialogoFitness == 3) {
                            nomePersonagem = "Nicollas";
                            textoDialogo = "Sempre que você fizer 12 polichinelos na sua vida real...";
                            faseDialogoFitness = 4;
                        } else if (faseDialogoFitness == 4) {
                            nomePersonagem = "Nicollas";
                            textoDialogo = "abre o seu diário e marca como concluído. Seja honesta!";
                            missaoFitnessAtiva = true;
                            faseDialogoFitness = 5;
                        }
                    } else if (missaoFitnessConcluida) {
                        nomePersonagem = "Nicollas";
                        textoDialogo = "Sua energia é contagiante! Parabéns!";
                        verificarLevel3();
                    } else {
                        nomePersonagem = "Nicollas";
                        textoDialogo = "Faça seus 12 polichinelos e marque no Diário (J)!";
                    }
                }
            }
        }

        if (code == Configuracoes.getInstance().getTecla("INTERAGIR")) {
            // Pegar livro no armário
            if (armarioEstaAberto && !livroJoiFoiPego) {
                GerenciadorAudio.tocarSomColeta();
                temLivro = true;
                livroJoiFoiPego = true;
                return;
            }

            // Sair da sala de aula
            if (indiceMapa == 2 && estaProximoDaPuerta()) {
                if (missaoDesenhoAtiva && !desenhoEntregue) {
                    GerenciadorAudio.tocarSomErro();
                    estaEmDialogoNicolas = true;
                    nomePersonagem = "Audrey";
                    textoDialogo = "Não posso sair agora, preciso terminar o desenho da missão.";
                    return;
                }
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
                if (!sala1Aberta) {
                    GerenciadorAudio.tocarSomErro();
                    estaEmDialogoNicolas = true;
                    nomePersonagem = "Audrey";
                    textoDialogo = "Ainda preciso conhecer o lugar e falar com o pessoal antes de entrar.";
                    return;
                }
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
            // Sair da biblioteca
            else if (indiceMapa == 6 && Math.abs(audreyX - 50) < 150) {
                GerenciadorAudio.tocarSomPortaMadeira();
                indiceMapa = 3;
                audreyX = 750;
            }
            // Entrar na biblioteca
            else if (indiceMapa == 3 && Math.abs(audreyX - 750) < 150) {
                GerenciadorAudio.tocarSomPortaMadeira();
                indiceMapa = 6;
                audreyX = 100;
                if (!ep1InteragiuBiblioteca) {
                    ep1InteragiuBiblioteca = true;
                    checarObjetivosEp1();
                }
            }
            // Mural
            else if (indiceMapa == 0 && Math.abs(audreyX - 250) < 100 && textoDialogo.isEmpty()) {
                if (!ep1InteragiuMural) {
                    GerenciadorAudio.tocarSomDialogo();
                    ep1InteragiuMural = true;
                    estaEmDialogoNicolas = true;
                    nomePersonagem = "Audrey";
                    textoDialogo = "Um mural de avisos da escola. 'Festival Cultural em breve!'";
                    checarObjetivosEp1();
                    return;
                }
            }
            // Porta Trancada
            else if (indiceMapa == 3 && Math.abs(audreyX - 350) < 150 && textoDialogo.isEmpty()) {
                GerenciadorAudio.tocarSomErro();
                nomePersonagem = "Audrey";
                textoDialogo = "A porta está trancada. Parece ser a sala da coordenação.";
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

        if (code == Configuracoes.getInstance().getTecla("FECHAR")) {
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

class Configuracoes {
    private static Configuracoes instancia;
    private java.util.HashMap<String, Integer> teclas;
    private int volumeEfeitos = 50;
    private int brilho = 50;
    private boolean shaderAtivo = false;

    public static final String[] ACOES = {
            "ESQUERDA", "DIREITA", "INTERAGIR", "FALAR",
            "FECHAR", "INVENTARIO", "OBJETIVOS", "DIARIO", "MENU"
    };
    public static final String[] LABELS = {
            "Mover Esquerda", "Mover Direita", "Interagir", "Falar",
            "Fechar Dialogo", "Inventario", "Objetivos", "Diario", "Menu"
    };

    private Configuracoes() {
        teclas = new java.util.HashMap<>();
        resetarTeclas();
        carregarConfiguracoes();
    }

    public static Configuracoes getInstance() {
        if (instancia == null)
            instancia = new Configuracoes();
        return instancia;
    }

    public void resetarTeclas() {
        teclas.put("ESQUERDA", KeyEvent.VK_A);
        teclas.put("DIREITA", KeyEvent.VK_D);
        teclas.put("INTERAGIR", KeyEvent.VK_E);
        teclas.put("FALAR", KeyEvent.VK_F);
        teclas.put("FECHAR", KeyEvent.VK_Q);
        teclas.put("INVENTARIO", KeyEvent.VK_B);
        teclas.put("OBJETIVOS", KeyEvent.VK_M);
        teclas.put("DIARIO", KeyEvent.VK_J);
        teclas.put("MENU", KeyEvent.VK_ESCAPE);
    }

    public int getTecla(String acao) {
        return teclas.getOrDefault(acao, 0);
    }

    public void setTecla(String acao, int keyCode) {
        teclas.put(acao, keyCode);
    }

    public String getNomeTecla(String acao) {
        return KeyEvent.getKeyText(getTecla(acao));
    }

    public int getVolumeEfeitos() {
        return volumeEfeitos;
    }

    public void setVolumeEfeitos(int v) {
        volumeEfeitos = Math.max(0, Math.min(100, v));
    }

    public int getBrilho() {
        return brilho;
    }

    public void setBrilho(int b) {
        brilho = Math.max(0, Math.min(100, b));
    }

    public boolean isShaderAtivo() {
        return shaderAtivo;
    }

    public void setShaderAtivo(boolean ativo) {
        shaderAtivo = ativo;
    }

    public void salvarConfiguracoes() {
        java.util.Properties props = new java.util.Properties();
        for (java.util.Map.Entry<String, Integer> entry : teclas.entrySet()) {
            props.setProperty("tecla_" + entry.getKey(), String.valueOf(entry.getValue()));
        }
        props.setProperty("volumeEfeitos", String.valueOf(volumeEfeitos));
        props.setProperty("brilho", String.valueOf(brilho));
        props.setProperty("shaderAtivo", String.valueOf(shaderAtivo));

        try (java.io.FileOutputStream fos = new java.io.FileOutputStream("config.properties")) {
            props.store(fos, "Configuracoes do Jogo");
        } catch (Exception e) {
            System.err.println("Erro ao salvar config: " + e.getMessage());
        }
    }

    public void carregarConfiguracoes() {
        java.util.Properties props = new java.util.Properties();
        try (java.io.FileInputStream fis = new java.io.FileInputStream("config.properties")) {
            props.load(fis);
            
            for (String acao : ACOES) {
                if (props.containsKey("tecla_" + acao)) {
                    teclas.put(acao, Integer.parseInt(props.getProperty("tecla_" + acao)));
                }
            }
            if (props.containsKey("volumeEfeitos")) volumeEfeitos = Integer.parseInt(props.getProperty("volumeEfeitos"));
            if (props.containsKey("brilho")) brilho = Integer.parseInt(props.getProperty("brilho"));
            if (props.containsKey("shaderAtivo")) shaderAtivo = Boolean.parseBoolean(props.getProperty("shaderAtivo"));
            
        } catch (Exception e) {
            System.err.println("Configuracoes nao encontradas ou erro ao carregar: " + e.getMessage());
        }
    }
}

class ConfiguracoesPanel extends JPanel implements KeyListener {
    private JogoAudrey frame;
    private String origem = "menuPrincipal";
    private Font fontCrayonHand, fontTitulo;
    private int abaAtual = 0;

    private JButton btnAbaControle, btnAbaSom, btnAbaTela;
    private JButton btnVoltar, btnResetar;
    private BotaoEstilizado btnShader;

    private JButton[] btnTeclas;
    private int esperandoTeclaIndex = -1;

    private JSlider sliderEfeitos;
    private JSlider sliderBrilho;

    public ConfiguracoesPanel(JogoAudrey frame) {
        this.frame = frame;
        setLayout(null);
        setFocusable(true);
        addKeyListener(this);
        carregarFonts();
        criarComponentes();
    }

    public void setOrigem(String origem) {
        this.origem = origem;
        abaAtual = 0;
        esperandoTeclaIndex = -1;
        atualizarBotoesTeclas();
        if (btnShader != null) {
            btnShader.setText(
                    "SHADER PASTEL: " + (Configuracoes.getInstance().isShaderAtivo() ? "LIGADO" : "DESLIGADO"));
        }
        sliderEfeitos.setValue(Configuracoes.getInstance().getVolumeEfeitos());
        sliderBrilho.setValue(Configuracoes.getInstance().getBrilho());
        atualizarAba();
    }

    private void carregarFonts() {
        try {
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf"))
                    .deriveFont(22f);
            fontTitulo = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf"))
                    .deriveFont(52f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontCrayonHand);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontTitulo);
        } catch (Exception e) {
            fontCrayonHand = new Font("Arial", Font.BOLD, 22);
            fontTitulo = new Font("Arial", Font.BOLD, 52);
        }
    }

    private void criarComponentes() {
        btnAbaControle = new BotaoEstilizado("CONTROLE", fontCrayonHand.deriveFont(20f));
        btnAbaControle.addActionListener(e -> {
            abaAtual = 0;
            esperandoTeclaIndex = -1;
            atualizarAba();
        });
        add(btnAbaControle);

        btnAbaSom = new BotaoEstilizado("SOM", fontCrayonHand.deriveFont(20f));
        btnAbaSom.addActionListener(e -> {
            abaAtual = 1;
            esperandoTeclaIndex = -1;
            atualizarAba();
        });
        add(btnAbaSom);

        btnAbaTela = new BotaoEstilizado("TELA", fontCrayonHand.deriveFont(20f));
        btnAbaTela.addActionListener(e -> {
            abaAtual = 2;
            esperandoTeclaIndex = -1;
            atualizarAba();
        });
        add(btnAbaTela);

        String[] acoes = Configuracoes.ACOES;
        btnTeclas = new JButton[acoes.length];
        for (int i = 0; i < acoes.length; i++) {
            final int idx = i;
            btnTeclas[i] = new BotaoEstilizado(Configuracoes.getInstance().getNomeTecla(acoes[i]),
                    fontCrayonHand.deriveFont(18f));
            btnTeclas[i].addActionListener(e -> {
                esperandoTeclaIndex = idx;
                btnTeclas[idx].setText("...");
                ConfiguracoesPanel.this.requestFocusInWindow();
            });
            add(btnTeclas[i]);
        }

        btnResetar = new BotaoEstilizado("RESETAR PADRAO", fontCrayonHand.deriveFont(18f));
        btnResetar.addActionListener(e -> {
            Configuracoes.getInstance().resetarTeclas();
            atualizarBotoesTeclas();
            repaint();
        });
        add(btnResetar);

        sliderEfeitos = new JSlider(0, 100, Configuracoes.getInstance().getVolumeEfeitos());
        sliderEfeitos.setOpaque(false);
        sliderEfeitos.setFont(fontCrayonHand.deriveFont(14f));
        sliderEfeitos.setMajorTickSpacing(25);
        sliderEfeitos.setPaintTicks(true);
        sliderEfeitos.setPaintLabels(true);
        sliderEfeitos.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                Configuracoes.getInstance().setVolumeEfeitos(sliderEfeitos.getValue());
                GerenciadorAudio.atualizarVolumeMusica(sliderEfeitos.getValue());
                repaint();
            }
        });
        add(sliderEfeitos);

        sliderBrilho = new JSlider(0, 100, Configuracoes.getInstance().getBrilho());
        sliderBrilho.setOpaque(false);
        sliderBrilho.setFont(fontCrayonHand.deriveFont(14f));
        sliderBrilho.setMajorTickSpacing(25);
        sliderBrilho.setPaintTicks(true);
        sliderBrilho.setPaintLabels(true);
        sliderBrilho.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                Configuracoes.getInstance().setBrilho(sliderBrilho.getValue());
                repaint();
            }
        });
        add(sliderBrilho);

        btnShader = new BotaoEstilizado(
                "SHADER PASTEL: " + (Configuracoes.getInstance().isShaderAtivo() ? "LIGADO" : "DESLIGADO"),
                fontCrayonHand.deriveFont(18f));
        btnShader.addActionListener(e -> {
            boolean ativo = !Configuracoes.getInstance().isShaderAtivo();
            Configuracoes.getInstance().setShaderAtivo(ativo);
            btnShader.setText("SHADER PASTEL: " + (ativo ? "LIGADO" : "DESLIGADO"));
            repaint();
        });
        add(btnShader);

        btnVoltar = new BotaoEstilizado("VOLTAR", fontCrayonHand);
        btnVoltar.addActionListener(e -> {
            Configuracoes.getInstance().salvarConfiguracoes();
            esperandoTeclaIndex = -1;
            if ("menuEmJogo".equals(origem)) {
                frame.mostrarMenuEmJogo();
            } else {
                frame.mostrarMenuPrincipal();
            }
        });
        add(btnVoltar);

        atualizarAba();
    }

    private void atualizarBotoesTeclas() {
        for (int i = 0; i < Configuracoes.ACOES.length; i++) {
            btnTeclas[i].setText(Configuracoes.getInstance().getNomeTecla(Configuracoes.ACOES[i]));
        }
    }

    private void atualizarAba() {
        for (JButton btn : btnTeclas)
            btn.setVisible(abaAtual == 0);
        btnResetar.setVisible(abaAtual == 0);
        sliderEfeitos.setVisible(abaAtual == 1);
        sliderBrilho.setVisible(abaAtual == 2);
        if (btnShader != null)
            btnShader.setVisible(abaAtual == 2);
        revalidate();
        repaint();
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0)
            return;

        int tabW = Math.max(160, w / 5);
        int tabH = 50;
        int tabY = (int) (h * 0.16);
        int totalTabW = tabW * 3 + 20;
        int tabStartX = (w - totalTabW) / 2;

        btnAbaControle.setBounds(tabStartX, tabY, tabW, tabH);
        btnAbaSom.setBounds(tabStartX + tabW + 10, tabY, tabW, tabH);
        btnAbaTela.setBounds(tabStartX + 2 * (tabW + 10), tabY, tabW, tabH);

        int contentY = tabY + tabH + 30;
        int contentW = Math.max(500, (int) (w * 0.55));
        int contentX = (w - contentW) / 2;

        int rowH = 45;
        int gap = 6;
        int btnW = contentW / 2 - 20;
        for (int i = 0; i < btnTeclas.length; i++) {
            btnTeclas[i].setBounds(contentX + contentW / 2 + 10, contentY + i * (rowH + gap), btnW, rowH);
        }
        btnResetar.setBounds(contentX, contentY + btnTeclas.length * (rowH + gap) + 10, contentW, rowH);

        int sliderW = Math.max(400, contentW - 100);
        int sliderX = (w - sliderW) / 2;
        sliderEfeitos.setBounds(sliderX, contentY + 60, sliderW, 60);
        sliderBrilho.setBounds(sliderX, contentY + 60, sliderW, 60);
        if (btnShader != null)
            btnShader.setBounds(sliderX, contentY + 140, sliderW, 50);

        int voltarW = Math.max(200, w / 5);
        int voltarH = 55;
        btnVoltar.setBounds((w - voltarW) / 2, h - voltarH - 50, voltarW, voltarH);
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

        int titleY = (int) (H * 0.10);
        g2d.setFont(fontTitulo);
        String titulo = "CONFIGURACOES";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (W - fm.stringWidth(titulo)) / 2;

        g2d.setColor(new Color(200, 150, 180, 120));
        g2d.drawString(titulo, x + 4, titleY + 4);

        GradientPaint titleGrad = new GradientPaint(x, titleY - 30, new Color(255, 150, 180), x, titleY,
                new Color(180, 230, 180));
        g2d.setPaint(titleGrad);
        g2d.drawString(titulo, x, titleY);

        int tabW = Math.max(160, W / 5);
        int tabH = 50;
        int tabY = (int) (H * 0.16);
        int totalTabW = tabW * 3 + 20;
        int tabStartX = (W - totalTabW) / 2;
        int indicatorX = tabStartX + abaAtual * (tabW + 10);
        g2d.setColor(new Color(255, 150, 180));
        g2d.fillRoundRect(indicatorX + 10, tabY + tabH, tabW - 20, 5, 3, 3);

        int contentY = tabY + tabH + 30;
        int contentW = Math.max(500, (int) (W * 0.55));
        int contentX = (W - contentW) / 2;

        g2d.setColor(new Color(120, 80, 100));

        if (abaAtual == 0) {
            int rowH = 45;
            int gapRow = 6;
            for (int i = 0; i < Configuracoes.LABELS.length; i++) {
                int textY = contentY + i * (rowH + gapRow) + rowH / 2 + 7;
                g2d.setFont(fontCrayonHand.deriveFont(20f));
                g2d.setColor(new Color(120, 80, 100));
                g2d.drawString(Configuracoes.LABELS[i], contentX, textY);
            }
            if (esperandoTeclaIndex >= 0) {
                g2d.setFont(fontCrayonHand.deriveFont(Font.ITALIC, 16f));
                g2d.setColor(new Color(200, 100, 100));
                int msgY = contentY + Configuracoes.LABELS.length * (rowH + gapRow) - 5;
                g2d.drawString("Pressione uma tecla para configurar...", contentX, msgY);
            }
        } else if (abaAtual == 1) {
            g2d.setFont(fontCrayonHand.deriveFont(Font.BOLD, 26f));
            g2d.drawString("Efeitos Sonoros", contentX, contentY + 40);
            g2d.setFont(fontCrayonHand.deriveFont(20f));
            String volText = Configuracoes.getInstance().getVolumeEfeitos() + "%";
            int volTextW = g2d.getFontMetrics().stringWidth(volText);
            g2d.drawString(volText, (W - volTextW) / 2, contentY + 140);
        } else if (abaAtual == 2) {
            g2d.setFont(fontCrayonHand.deriveFont(Font.BOLD, 26f));
            g2d.drawString("Brilho da Tela", contentX, contentY + 40);
            g2d.setFont(fontCrayonHand.deriveFont(20f));
            String brilhoText = Configuracoes.getInstance().getBrilho() + "%";
            int brilhoTextW = g2d.getFontMetrics().stringWidth(brilhoText);
            g2d.drawString(brilhoText, (W - brilhoTextW) / 2, contentY + 140);

            int previewX = (W - 200) / 2;
            int previewY = contentY + 160;
            g2d.setColor(new Color(100, 180, 255));
            g2d.fillRoundRect(previewX, previewY, 200, 100, 15, 15);
            g2d.setFont(fontCrayonHand.deriveFont(14f));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Preview", previewX + 75, previewY + 55);

            int brilhoVal = Configuracoes.getInstance().getBrilho();
            if (brilhoVal != 50) {
                if (brilhoVal < 50) {
                    int alphaB = (int) ((50 - brilhoVal) / 50.0 * 180);
                    g2d.setColor(new Color(0, 0, 0, Math.min(alphaB, 255)));
                } else {
                    int alphaB = (int) ((brilhoVal - 50) / 50.0 * 100);
                    g2d.setColor(new Color(255, 255, 255, Math.min(alphaB, 255)));
                }
                g2d.fillRoundRect(previewX, previewY, 200, 100, 15, 15);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (esperandoTeclaIndex >= 0) {
            int keyCode = e.getKeyCode();
            String acao = Configuracoes.ACOES[esperandoTeclaIndex];
            Configuracoes.getInstance().setTecla(acao, keyCode);
            btnTeclas[esperandoTeclaIndex].setText(KeyEvent.getKeyText(keyCode));
            esperandoTeclaIndex = -1;
            repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
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
