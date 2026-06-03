import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class JogoAudrey extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private MenuPrincipal menuPrincipal;
    private MenuEmJogo menuEmJogo;
    private JogoPanel jogoPanel;
    private CutscenePanel cutscenePanel;
    private TelaCarregamento telaCarregamento;
    private boolean jogoIniciado = false;

    public JogoAudrey() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salvarESair();
            }
        });
        setTitle("Audrey Adventure");
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        cutscenePanel = new CutscenePanel(this);
        jogoPanel = new JogoPanel(this);
        menuPrincipal = new MenuPrincipal(this);
        menuEmJogo = new MenuEmJogo(this, jogoPanel);
        telaCarregamento = new TelaCarregamento(this);
        
        mainPanel.add(telaCarregamento, "carregamento");
        mainPanel.add(cutscenePanel, "cutscene");
        mainPanel.add(menuPrincipal, "menuPrincipal");
        mainPanel.add(jogoPanel, "jogo");
        mainPanel.add(menuEmJogo, "menuEmJogo");
        
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        
        cardLayout.show(mainPanel, "menuPrincipal");
    }

    public void iniciarJogo() {
        jogoIniciado = true;
        GerenciadorAudio.tocarSomPlay();
        cardLayout.show(mainPanel, "carregamento");
        telaCarregamento.iniciarCarregamento();
    }

    public void irParaCutscene() {
        cutscenePanel = new CutscenePanel(this);
        mainPanel.remove(cutscenePanel);
        cutscenePanel = new CutscenePanel(this);
        mainPanel.add(cutscenePanel, "cutscene");
        mainPanel.revalidate();
        cardLayout.show(mainPanel, "cutscene");
        cutscenePanel.requestFocus();
    }

    public void irParaJogoAposCutscene() {
        recriarJogo();
        cardLayout.show(mainPanel, "jogo");
        jogoPanel.requestFocus();
    }

    public void mostrarMenuEmJogo() {
        GerenciadorAudio.tocarSomDialogo();
        cardLayout.show(mainPanel, "menuEmJogo");
    }

    public void continuarJogo() {
        cardLayout.show(mainPanel, "jogo");
        jogoPanel.requestFocus();
    }

    public void continuarJogoSalvo() {
        if (Database.saveExiste()) {
            jogoIniciado = true;
            recriarJogo();
            java.util.Properties props = Database.carregarEstado();
            if (props != null) {
                jogoPanel.carregarEstado(props);
            }
            cardLayout.show(mainPanel, "jogo");
            jogoPanel.requestFocus();
        }
    }

    public void voltarAoMenuPrincipal() {
        if (jogoIniciado && jogoPanel != null) {
            jogoPanel.salvarEstado();
        }
        jogoIniciado = false;
        mostrarMenuPrincipal();
    }

    public void salvarESair() {
        if (jogoIniciado && jogoPanel != null) {
            jogoPanel.salvarEstado();
        }
        System.exit(0);
    }

    public void mostrarMenuPrincipal() {
        menuPrincipal.atualizarBotoes();
        cardLayout.show(mainPanel, "menuPrincipal");
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
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf")).deriveFont(28f);
            fontTitulo = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf")).deriveFont(72f);
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
            frame.irParaCutscene();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fundo gradiente escuro roxo
        GradientPaint bg = new GradientPaint(
            0, 0, new Color(15, 5, 35),
            LARGURA, ALTURA, new Color(50, 15, 70)
        );
        g2d.setPaint(bg);
        g2d.fillRect(0, 0, LARGURA, ALTURA);

        // Estrelas animadas de fundo
        for (int i = 0; i < 60; i++) {
            float cx2 = (float)((Math.sin(i * 1.7 + anguloAnimacao * 0.15) + 1.0) * 500);
            float cy2 = (float)((Math.cos(i * 2.3 + anguloAnimacao * 0.1) + 1.0) * 375);
            int sz = (i % 3 == 0) ? 3 : 2;
            int alpha = 60 + (i % 4) * 30;
            g2d.setColor(new Color(220, 200, 255, Math.min(alpha, 180)));
            g2d.fillOval((int)cx2, (int)cy2, sz, sz);
        }

        // Brilho central suave
        RadialGradientPaint glow = new RadialGradientPaint(
            LARGURA / 2f, ALTURA / 2f, 350,
            new float[]{0f, 1f},
            new Color[]{new Color(120, 60, 200, 40), new Color(0, 0, 0, 0)}
        );
        g2d.setPaint(glow);
        g2d.fillRect(0, 0, LARGURA, ALTURA);

        // Título
        g2d.setFont(fontTitulo);
        String titulo = "AUDREY ADVENTURE";
        FontMetrics fmT = g2d.getFontMetrics();
        int tx = (LARGURA - fmT.stringWidth(titulo)) / 2;

        // Sombra do título
        g2d.setColor(new Color(180, 80, 255, 120));
        g2d.drawString(titulo, tx + 5, 255);

        // Título com gradiente
        GradientPaint titleGrad = new GradientPaint(
            tx, 180, new Color(255, 200, 255),
            tx + fmT.stringWidth(titulo), 250, new Color(160, 100, 255)
        );
        g2d.setPaint(titleGrad);
        g2d.drawString(titulo, tx, 250);

        // Subtítulo
        g2d.setFont(fontCrayonHand.deriveFont(22f));
        g2d.setColor(new Color(200, 170, 255));
        String sub = "Preparando a aventura...";
        FontMetrics fmS = g2d.getFontMetrics();
        g2d.drawString(sub, (LARGURA - fmS.stringWidth(sub)) / 2, 320);

        // --- Barra de progresso ---
        int barX = 200;
        int barY = 430;
        int barW = 600;
        int barH = 28;

        // Fundo da barra
        g2d.setColor(new Color(40, 20, 70));
        g2d.fillRoundRect(barX, barY, barW, barH, 14, 14);
        g2d.setColor(new Color(100, 60, 160));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(barX, barY, barW, barH, 14, 14);

        // Preenchimento da barra
        int barFill = (int)(barW * progresso / 100.0);
        if (barFill > 0) {
            GradientPaint barGrad = new GradientPaint(
                barX, barY, new Color(180, 80, 255),
                barX + barFill, barY, new Color(255, 130, 220)
            );
            g2d.setPaint(barGrad);
            g2d.fillRoundRect(barX, barY, barFill, barH, 14, 14);
            // Reflexo brilhante na barra
            g2d.setColor(new Color(255, 255, 255, 50));
            g2d.fillRoundRect(barX + 2, barY + 2, barFill - 4, barH / 2 - 2, 10, 10);
        }

        // Porcentagem
        g2d.setFont(fontCrayonHand.deriveFont(20f));
        g2d.setColor(Color.WHITE);
        String pct = progresso + "%";
        FontMetrics fmP = g2d.getFontMetrics();
        g2d.drawString(pct, (LARGURA - fmP.stringWidth(pct)) / 2, barY + barH + 38);

        // Spinner animado
        int spinX = LARGURA / 2;
        int spinY = 570;
        int r = 18;
        g2d.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 8; i++) {
            float angle = (float)(i * Math.PI / 4 + anguloAnimacao);
            float alpha2 = (i + 1) / 8.0f;
            g2d.setColor(new Color(200, 140, 255, (int)(alpha2 * 255)));
            int x1 = spinX + (int)(Math.cos(angle) * (r - 6));
            int y1 = spinY + (int)(Math.sin(angle) * (r - 6));
            int x2 = spinX + (int)(Math.cos(angle) * r);
            int y2 = spinY + (int)(Math.sin(angle) * r);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Texto "Carregando..."
        g2d.setFont(fontCrayonHand.deriveFont(16f));
        g2d.setColor(new Color(180, 150, 230));
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
        corPrincipal = new Color(200, 220, 180); // Verde pastel olivado
        setPreferredSize(new Dimension(1000, 750));
        setBackground(new Color(245, 242, 235)); // Bege suave
        setLayout(null);

        carregarFonts();
        criarComponentes();
    }

    private void carregarFonts() {
        try {
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf")).deriveFont(24f);
            fontTitulo = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf")).deriveFont(72f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontCrayonHand);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontTitulo);
        } catch (Exception e) {
            fontCrayonHand = new Font("Arial", Font.BOLD, 24);
            fontTitulo = new Font("Arial", Font.BOLD, 72);
        }
    }

    private void criarComponentes() {
        btnPlay = criarBotao("NOVO JOGO", 350, 310, 300, 70);
        btnPlay.addActionListener(e -> frame.iniciarJogo());
        add(btnPlay);

        btnContinuar = criarBotao("CONTINUAR", 350, 400, 300, 70);
        btnContinuar.addActionListener(e -> frame.continuarJogoSalvo());
        add(btnContinuar);

        btnSobre = criarBotao("SOBRE O JOGO", 350, 490, 300, 70);
        btnSobre.addActionListener(e -> mostrarSobre());
        add(btnSobre);

        btnSair = criarBotao("SAIR", 350, 580, 300, 70);
        btnSair.addActionListener(e -> frame.salvarESair());
        add(btnSair);
        
        atualizarBotoes();
    }

    public void atualizarBotoes() {
        if (btnContinuar != null) {
            boolean existe = Database.saveExiste();
            btnContinuar.setEnabled(existe);
            btnContinuar.setBackground(existe ? corPrincipal : Color.GRAY);
        }
    }

    private JButton criarBotao(String texto, int x, int y, int largura, int altura) {
        JButton botao = new JButton(texto);
        botao.setBounds(x, y, largura, altura);
        botao.setFont(fontCrayonHand);
        botao.setBackground(corPrincipal);
        botao.setForeground(new Color(100, 110, 90));
        botao.setBorder(BorderFactory.createRaisedBevelBorder());
        botao.setFocusPainted(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));

        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setBackground(new Color(210, 230, 190));
                botao.setFont(fontCrayonHand.deriveFont(Font.BOLD));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(corPrincipal);
                botao.setFont(fontCrayonHand);
            }
        });

        return botao;
    }

    private void mostrarSobre() {
        String mensagem = "AUDREY ADVENTURE\n\n" +
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
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(245, 242, 235),
                0, getHeight(), new Color(220, 230, 210)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setFont(fontTitulo);
        g2d.setColor(corPrincipal);
        String titulo = "AUDREY ADVENTURE";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(titulo)) / 2;
        g2d.drawString(titulo, x, 120);

        g2d.setColor(new Color(200, 210, 190));
        g2d.drawString(titulo, x + 3, 123);
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
        corPrincipal = new Color(200, 220, 180); // Verde pastel olivado
        setPreferredSize(new Dimension(1000, 750));
        setBackground(new Color(245, 242, 235)); // Bege suave
        setLayout(null);

        carregarFonts();
        criarComponentes();
    }

    private void carregarFonts() {
        try {
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf")).deriveFont(24f);
            fontTitulo = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf")).deriveFont(60f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontCrayonHand);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontTitulo);
        } catch (Exception e) {
            fontCrayonHand = new Font("Arial", Font.BOLD, 24);
            fontTitulo = new Font("Arial", Font.BOLD, 60);
        }
    }

    private void criarComponentes() {
        btnContinuar = criarBotao("VOLTAR AO JOGO", 350, 240, 300, 70);
        btnContinuar.addActionListener(e -> frame.continuarJogo());
        add(btnContinuar);

        btnSalvar = criarBotao("SALVAR JOGO", 350, 330, 300, 70);
        btnSalvar.addActionListener(e -> {
            jogoPanel.salvarEstado();
            JOptionPane.showMessageDialog(this, "Jogo salvo com sucesso!");
        });
        add(btnSalvar);

        btnSobre = criarBotao("SOBRE O JOGO", 350, 420, 300, 70);
        btnSobre.addActionListener(e -> mostrarSobre());
        add(btnSobre);

        btnMenuPrincipal = criarBotao("MENU PRINCIPAL", 350, 510, 300, 70);
        btnMenuPrincipal.addActionListener(e -> frame.voltarAoMenuPrincipal());
        add(btnMenuPrincipal);

        btnSair = criarBotao("SAIR", 350, 600, 300, 70);
        btnSair.addActionListener(e -> frame.salvarESair());
        add(btnSair);
    }

    private JButton criarBotao(String texto, int x, int y, int largura, int altura) {
        JButton botao = new JButton(texto);
        botao.setBounds(x, y, largura, altura);
        botao.setFont(fontCrayonHand);
        botao.setBackground(corPrincipal);
        botao.setForeground(new Color(100, 110, 90));
        botao.setBorder(BorderFactory.createRaisedBevelBorder());
        botao.setFocusPainted(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));

        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setBackground(new Color(210, 230, 190));
                botao.setFont(fontCrayonHand.deriveFont(Font.BOLD));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(corPrincipal);
                botao.setFont(fontCrayonHand);
            }
        });

        return botao;
    }

    private void mostrarSobre() {
        String mensagem = "AUDREY ADVENTURE\n\n" +
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
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(245, 242, 235),
                0, getHeight(), new Color(220, 230, 210)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setFont(fontTitulo);
        g2d.setColor(corPrincipal);
        String titulo = "PAUSADO";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(titulo)) / 2;
        g2d.drawString(titulo, x, 150);

        g2d.setColor(new Color(200, 210, 190));
        g2d.drawString(titulo, x + 3, 153);
    }
}

class JogoPanel extends JPanel implements ActionListener, KeyListener {
    private JogoAudrey frame;
    private final int LARGURA = 1000;
    private final int ALTURA = 750;

    private Image fundoCenario1, fundoCenario2, fundoCenario3, imgArmarioAberto, imgNicolas;
    private Image imgChave, imgLivro;
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
    private boolean mostrando_chave = false;
    private boolean inventarioAberto = false;
    private boolean temLivro = false;
    private boolean livroJoiFoiPego = false;
    private boolean mostrarObjetivos = false;
    private int contadorEfeitoChave = 0;

    private int indiceMapa = 0, faseDialogoNicolas = 0;
    private int ultimaPosAoEntraSala = 0;

    private int posArmarioX = 650;
    private int posNicolasXSalaAula = 800;
    private int posPortaX = 500;

    private int puertaSaidaX = 20;
    private int puertaSaidaLargura = 80;

    private String textoDialogo = "";
    private String nomePersonagem = "";

    private Font fontCrayonHand;

    public int indiceMapa_public = 0;
    public int audreyX_public = 100;

    public void salvarEstado() {
        Database.salvarEstado(indiceMapa, audreyX, temChave, armarioEstaAberto, 
                              nicolasJaFoiEncontrado, livroJoiFoiPego, temLivro, faseDialogoNicolas);
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
        
        textoDialogo = "";
        nomePersonagem = "";
        estaEmDialogoNicolas = false;
        inventarioAberto = false;
        mostrarObjetivos = false;
        repaint();
    }

    public JogoPanel(JogoAudrey frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setFocusable(true);
        addKeyListener(this);
        setDoubleBuffered(true);

        carregarFonts();
        carregarAssets();
        new Timer(16, this).start();
    }

    private void carregarFonts() {
        try {
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf")).deriveFont(14f);
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

        framesAndar[0] = redimensionarImagem(new ImageIcon("1-removebg-preview3.png").getImage(), AUDREY_LARGURA, AUDREY_ALTURA);
        framesAndar[1] = redimensionarImagem(new ImageIcon("2-removebg-preview65.png").getImage(), AUDREY_LARGURA, AUDREY_ALTURA);

        imgParada = redimensionarImagem(new ImageIcon("Parada_fundo_verde-removebg-preview24.png").getImage(), AUDREY_LARGURA, AUDREY_ALTURA);
    }

    private Image redimensionarImagem(Image img, int width, int height) {
        return img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

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
                    g2d.drawImage(imgNicolas, posNicolasXSalaAula - NICOLAS_LARGURA / 2, audreyY - NICOLAS_ALTURA, NICOLAS_LARGURA, NICOLAS_ALTURA, this);
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
                g2d.drawImage(img, audreyX + AUDREY_LARGURA, audreyY - AUDREY_ALTURA, -AUDREY_LARGURA, AUDREY_ALTURA, this);
            }

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
                int lw = (int)(imgW * escala);
                int lh = (int)(imgH * escala);
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
            g2d.drawString("Obtida de: Nicolas", caixaX + 130, itemY + 35);

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
    }

    private void desenharEfeitoChave(Graphics2D g2d) {
        int x = LARGURA - 120;
        int y = 100;

        float alpha = 1.0f - (contadorEfeitoChave / 60.0f);

        if (alpha > 0) {
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            g2d.setComposite(ac);

            float escala = 1.0f + (contadorEfeitoChave / 30.0f);
            int tamanhoCh = (int)(80 * escala);

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
                desenharTextoComSombra(g2d, "Pressione [E] para entrar", audreyX - 10, audreyY - 430, Color.GREEN);
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
                    desenharTextoComSombra(g2d, "(Pressione [F] para próxima fala ou [Q] para fechar)", 80, caixaY + 105, Color.LIGHT_GRAY);
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
                if (audreyX < 0) {
                    audreyX = 0;
                }
                if (audreyX + AUDREY_LARGURA > LARGURA) {
                    audreyX = LARGURA - AUDREY_LARGURA;
                }
            }

            if (indiceMapa == 2 && Math.abs(audreyX - posNicolasXSalaAula) > 200) {
                if (!estaEmDialogoNicolas) {
                    textoDialogo = "";
                    nomePersonagem = "";
                    faseDialogoNicolas = 0;
                }
            }

            if (indiceMapa == 0 && Math.abs(audreyX - posArmarioX) > 200) {
                textoDialogo = "";
                nomePersonagem = "";
            }

            if (indiceMapa == 1 && Math.abs(audreyX - posPortaX) > 200) {
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

        if (code == KeyEvent.VK_F) {
            if (indiceMapa == 2 && Math.abs(audreyX - posNicolasXSalaAula) < 120) {
                estaEmDialogoNicolas = true;
                nomePersonagem = "Nicolas";

                if (!nicolasJaFoiEncontrado) {
                    if (faseDialogoNicolas == 0) {
                        GerenciadorAudio.tocarSomDialogo();
                        textoDialogo = "Oi Audrey! Você procura a chave do armário?";
                        faseDialogoNicolas = 1;
                    }
                    else if (faseDialogoNicolas == 1) {
                        GerenciadorAudio.tocarSomSucesso();
                        textoDialogo = "Aqui está! Boa sorte!";
                        temChave = true;
                        mostrando_chave = true;
                        nicolasJaFoiEncontrado = true;
                        faseDialogoNicolas = 2;
                    }
                    else if (faseDialogoNicolas == 2) {
                        textoDialogo = "Volte quando abrir o armário!";
                        faseDialogoNicolas = 3;
                    }
                }
                else if (livroJoiFoiPego) {
                    if (faseDialogoNicolas == 0 || faseDialogoNicolas == 3) {
                        GerenciadorAudio.tocarSomSucesso();
                        textoDialogo = "Parabéns! Você conseguiu abrir!";
                        faseDialogoNicolas = 4;
                    }
                    else if (faseDialogoNicolas == 4) {
                        textoDialogo = "Você foi muito corajosa!";
                        faseDialogoNicolas = 5;
                    }
                    else if (faseDialogoNicolas == 5) {
                        textoDialogo = "Parabéns por terminar a aventura!";
                        faseDialogoNicolas = 6;
                    }
                }
                else {
                    if (faseDialogoNicolas == 3) {
                        textoDialogo = "Você já abriu o armário com a chave?";
                        faseDialogoNicolas = 3;
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
                GerenciadorAudio.tocarSomPortaMadeira();
                ultimaPosAoEntraSala = audreyX;
                indiceMapa = 2;
                audreyX = 100;
                textoDialogo = "";
                nomePersonagem = "";
                estaEmDialogoNicolas = false;
                faseDialogoNicolas = 0;
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
                    textoDialogo = "Está trancado... preciso da chave de Nicolas.";
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
    public void keyTyped(KeyEvent e) {}
}