
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;

public class JogoAudrey extends JFrame {

    private static java.util.Map<String, Font> fontCache = new java.util.HashMap<>();
    
    public static Font getCachedFont(Font baseFont, int style, float size) {
        if (baseFont == null) return null;
        String key = baseFont.getName() + "_" + style + "_" + size;
        Font f = fontCache.get(key);
        if (f == null) {
            f = baseFont.deriveFont(style, size);
            fontCache.put(key, f);
        }
        return f;
    }
    
    public static Font getCachedFont(Font baseFont, float size) {
        if (baseFont == null) return null;
        return JogoAudrey.getCachedFont(baseFont, baseFont.getStyle(), size);
    }

    public static Font getCachedFont(Font baseFont, int style) {
        if (baseFont == null) return null;
        return JogoAudrey.getCachedFont(baseFont, style, baseFont.getSize2D());
    }

    public static String resolvePath(String relativePath) {
        if (relativePath == null) {
            return null;
        }

        // List of subfolders to check
        String[] subfolders = {"", "imagens/", "audios/", "codigos/"};

        // 1. Try relative to the class folder (very robust for IDEs)
        try {
            java.io.File classDir = new java.io.File(JogoAudrey.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (classDir.isFile()) {
                classDir = classDir.getParentFile();
            }
            for (String sub : subfolders) {
                java.io.File targetFile = new java.io.File(classDir, sub + relativePath);
                if (targetFile.exists()) {
                    String absPath = targetFile.getAbsolutePath();
                    System.out.println("[DEBUG] resolvePath: " + relativePath + " -> (classDir + " + sub + ") " + absPath);
                    return absPath;
                }
            }
        } catch (Exception e) {
            // Ignore
        }

        // 2. Try directly in CWD and CWD subfolders
        for (String sub : subfolders) {
            java.io.File file = new java.io.File(sub + relativePath);
            if (file.exists()) {
                System.out.println("[DEBUG] resolvePath: " + relativePath + " -> (CWD + " + sub + ") " + file.getPath());
                return file.getPath();
            }
        }

        // 3. Try in jogomaneiro-main subfolder and its subfolders
        for (String sub : subfolders) {
            java.io.File subFile = new java.io.File("jogomaneiro-main/" + sub + relativePath);
            if (subFile.exists()) {
                String subPath = "jogomaneiro-main/" + sub + relativePath;
                System.out.println("[DEBUG] resolvePath: " + relativePath + " -> (subfolder exists + " + sub + ") " + subPath);
                return subPath;
            }
        }

        // 4. Check if the parent folder jogomaneiro-main exists, if so prefix it
        if (new java.io.File("jogomaneiro-main").isDirectory()) {
            String subPath = "jogomaneiro-main/" + relativePath;
            System.out.println("[DEBUG] resolvePath: " + relativePath + " -> (subfolder directory) " + subPath);
            return subPath;
        }

        System.out.println("[DEBUG] resolvePath: " + relativePath + " -> (fallback) " + relativePath);
        return relativePath;
    }

    public static void garantirImagemExiste(String relativePath) {
        String resolvedPath = resolvePath(relativePath);
        java.io.File file = new java.io.File(resolvedPath);
        if (file.exists()) {
            return;
        }

        java.io.File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        int width = 800;
        int height = 600;
        if (relativePath.contains("chave") || relativePath.contains("livro")) {
            width = 64;
            height = 64;
        }

        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (relativePath.endsWith("ginasio.png")) {
            g2d.setColor(new Color(40, 140, 80));
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(4f));
            g2d.drawRect(50, 50, width - 100, height - 100);
            g2d.drawOval(width / 2 - 80, height / 2 - 80, 160, 160);
            g2d.drawLine(width / 2, 50, width / 2, height - 50);

            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            g2d.drawString("GINÁSIO ESCOLAR", width / 2 - 170, height / 2 - 120);
        } else if (relativePath.endsWith("chave.png")) {
            g2d.setColor(new Color(0, 0, 0, 0));
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(new Color(255, 215, 0));
            g2d.fillOval(16, 16, 28, 28);
            g2d.setColor(new Color(0, 0, 0, 0));
            g2d.fillOval(22, 22, 16, 16);
            g2d.setColor(new Color(255, 215, 0));
            g2d.fillRect(26, 40, 8, 16);
            g2d.fillRect(34, 46, 8, 4);
            g2d.fillRect(34, 52, 8, 4);
        } else if (relativePath.endsWith("livro.png")) {
            g2d.setColor(new Color(0, 0, 0, 0));
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(new Color(41, 128, 185));
            g2d.fillRoundRect(12, 8, 40, 48, 6, 6);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(16, 12, 32, 40);
            g2d.setColor(new Color(41, 128, 185));
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString("JAVA", 20, 36);
        } else if (relativePath.contains("cutscene_sala_1")) {
            g2d.setColor(new Color(245, 230, 210));
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(new Color(139, 69, 19));
            g2d.fillRect(100, 320, 600, 180);
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 28));
            g2d.drawString("Cutscene: Sala de Aula", width / 2 - 150, height / 2 - 20);
        } else if (relativePath.contains("cutscene_sala_2")) {
            g2d.setColor(new Color(210, 225, 240));
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(150, 120, 90, 360);
            g2d.fillRect(270, 120, 90, 360);
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 28));
            g2d.drawString("Cutscene: Corredor da Escola", width / 2 - 200, height / 2 - 20);
        } else if (relativePath.contains("cutscene_final_1")) {
            g2d.setColor(new Color(255, 230, 230));
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            g2d.drawString("Fim da Jornada!", width / 2 - 140, height / 2 - 40);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("Audrey e seus amigos celebram a vitória!", width / 2 - 180, height / 2 + 20);
        } else if (relativePath.contains("cutscene_final_2")) {
            g2d.setColor(new Color(30, 15, 60));
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(new Color(255, 215, 0));
            g2d.setFont(new Font("Arial", Font.BOLD, 54));
            g2d.drawString("PARABÉNS!", width / 2 - 150, height / 2 - 50);
            g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Jogo finalizado com sucesso!", width / 2 - 160, height / 2 + 30);
        } else {
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Placeholder: " + relativePath, 30, height / 2);
        }

        g2d.dispose();
        try {
            javax.imageio.ImageIO.write(img, "png", file);
            System.out.println("[INFO] Gerada imagem ausente: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("[ERRO] Falha ao gerar imagem: " + relativePath + " - " + e.getMessage());
        }
    }

    private static Font fonteJogo = null;

    public static Font getFonteJogo(float tamanho) {
        if (fonteJogo == null) {
            try {
                fonteJogo = Font.createFont(Font.TRUETYPE_FONT,
                        new java.io.File(resolvePath("CrayonHandRegular2016Demo.ttf")));
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fonteJogo);
            } catch (Exception e) {
                fonteJogo = new Font("Arial", Font.PLAIN, 14);
            }
        }
        return JogoAudrey.getCachedFont(fonteJogo, tamanho);
    }

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
    private ImportadorImagem importadorImagem;

    public int getSlotAtual() {
        return this.slotAtual;
    }

    public JogoPanel getJogoPanel() {
        return this.jogoPanel;
    }

    public void mostrarImportador() {
        cardLayout.show(mainPanel, "importador");
        requestFocusInWindow();
    }

    public void mostrarImportadorReal() {
        mostrarImportador();
    }

    public void voltarAoJogo() {
        cardLayout.show(mainPanel, "jogo");
        requestFocusInWindow();
    }

    public JogoAudrey() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmarSaida();
            }
        });
        setTitle("hobby Quest");

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
        importadorImagem = new ImportadorImagem(this, () -> {
            cardLayout.show(mainPanel, "jogo");
            requestFocusInWindow();
            if (jogoPanel != null) {
                jogoPanel.continuarDialogoDesenho();
            }
        });
        mainPanel.add(importadorImagem, "importador");

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
                if (e.getID() == KeyEvent.KEY_PRESSED
                        && e.getKeyCode() == Configuracoes.getInstance().getTecla("MENU")) {
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
        recriarJogo(); // Garante que o painel sera zerado para um novo jogo
        jogoIniciado = true;
        cardLayout.show(mainPanel, "carregamento");
        telaCarregamento.iniciarCarregamento();
    }

    public void irParaCutscene(int idCutscene) {
        if (cutscenePanel != null) {
            mainPanel.remove(cutscenePanel);
        }
        cutscenePanel = new CutscenePanel(this);
        cutscenePanel.iniciarCutscene(idCutscene);
        mainPanel.add(cutscenePanel, "cutscene");
        mainPanel.revalidate();
        mainPanel.repaint();
        cardLayout.show(mainPanel, "cutscene");
        cutscenePanel.requestFocus();
    }

    public void voltarDeCutscene() {
        cardLayout.show(mainPanel, "jogo");
        jogoPanel.requestFocus();
    }

    public void mostrarMenuEmJogo() {
        GerenciadorAudio.tocarSomDialogo();
        GerenciadorAudio.pausarMusicaFundo();
        GerenciadorAudio.pararSomPassos();
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
        if (!jogoIniciado || jogoPanel == null) {
            System.exit(0);
            return;
        }

        JDialog dialog = new JDialog(this, "Sair do Jogo", true);
        dialog.setUndecorated(true);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(this);

        JPanel painel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                GradientPaint bg = new GradientPaint(0, 0, new Color(25, 15, 55), w, h, new Color(40, 20, 80));
                g2.setPaint(bg);
                g2.fillRoundRect(0, 0, w, h, 30, 30);
                g2.setColor(new Color(120, 80, 220, 200));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 2, h - 2, 30, 30);
                g2.setColor(new Color(100, 70, 180, 100));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(30, 80, w - 30, 80);
                g2.dispose();
            }
        };
        painel.setOpaque(false);

        JLabel lblTitulo = new JLabel("Sair do Jogo", SwingConstants.CENTER);
        lblTitulo.setFont(JogoAudrey.getFonteJogo(22f));
        lblTitulo.setForeground(new Color(220, 200, 255));
        lblTitulo.setBounds(0, 20, 420, 40);
        painel.add(lblTitulo);

        JLabel lblPerg = new JLabel("<html><div style='text-align:center'>Deseja salvar o progresso<br>antes de fechar o jogo?</div></html>", SwingConstants.CENTER);
        lblPerg.setFont(JogoAudrey.getFonteJogo(15f));
        lblPerg.setForeground(new Color(180, 160, 230));
        lblPerg.setBounds(0, 95, 420, 60);
        painel.add(lblPerg);

        // Botao Salvar e Sair
        JButton btnSalvar = new JButton("SALVAR E SAIR") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(80, 180, 100) : new Color(50, 140, 70));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(120, 230, 140, 180));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(Color.WHITE);
                g2.setFont(JogoAudrey.getFonteJogo(14f));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnSalvar.setContentAreaFilled(false);
        btnSalvar.setBorderPainted(false);
        btnSalvar.setFocusPainted(false);
        btnSalvar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSalvar.setBounds(20, 175, 170, 45);
        btnSalvar.addActionListener(e -> {
            dialog.dispose();
            if (slotAtual != -1) {
                jogoPanel.salvarEstado(slotAtual);
            }
            System.exit(0);
        });
        painel.add(btnSalvar);

        // Botao Sair sem salvar
        JButton btnSair = new JButton("SAIR SEM SALVAR") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(180, 60, 60) : new Color(140, 40, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(230, 120, 120, 180));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(Color.WHITE);
                g2.setFont(JogoAudrey.getFonteJogo(14f));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnSair.setContentAreaFilled(false);
        btnSair.setBorderPainted(false);
        btnSair.setFocusPainted(false);
        btnSair.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSair.setBounds(200, 175, 170, 45);
        btnSair.addActionListener(e -> {
            dialog.dispose();
            System.exit(0);
        });
        painel.add(btnSair);

        // Botao Cancelar
        JButton btnCancelar = new JButton("CANCELAR") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(100, 60, 200) : new Color(70, 40, 150));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(160, 130, 255, 180));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(Color.WHITE);
                g2.setFont(JogoAudrey.getFonteJogo(14f));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnCancelar.setContentAreaFilled(false);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelar.setBounds(110, 250, 200, 42);
        btnCancelar.addActionListener(e -> dialog.dispose());
        painel.add(btnCancelar);

        dialog.setContentPane(painel);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.getRootPane().setOpaque(false);
        dialog.setVisible(true);
    }

    public boolean mostrarConfirmacao(String titulo, String mensagem) {
        final boolean[] resposta = {false};
        JDialog dialog = new JDialog(this, titulo, true);
        dialog.setUndecorated(true);
        dialog.setSize(420, 260);
        dialog.setLocationRelativeTo(this);

        JPanel painel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                GradientPaint bg = new GradientPaint(0, 0, new Color(25, 15, 55), w, h, new Color(40, 20, 80));
                g2.setPaint(bg);
                g2.fillRoundRect(0, 0, w, h, 30, 30);
                g2.setColor(new Color(120, 80, 220, 200));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 2, h - 2, 30, 30);
                g2.setColor(new Color(100, 70, 180, 100));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(30, 60, w - 30, 60);
                g2.dispose();
            }
        };
        painel.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(JogoAudrey.getFonteJogo(22f));
        lblTitulo.setForeground(new Color(220, 200, 255));
        lblTitulo.setBounds(0, 15, 420, 40);
        painel.add(lblTitulo);

        String msgHtml = "<html><div style='text-align:center'>" + mensagem.replace("\n", "<br>") + "</div></html>";
        JLabel lblPerg = new JLabel(msgHtml, SwingConstants.CENTER);
        lblPerg.setFont(JogoAudrey.getFonteJogo(15f));
        lblPerg.setForeground(new Color(180, 160, 230));
        lblPerg.setBounds(20, 70, 380, 80);
        painel.add(lblPerg);

        JButton btnSim = new JButton("SIM") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(180, 60, 60) : new Color(140, 40, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(230, 120, 120, 180));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(Color.WHITE);
                g2.setFont(JogoAudrey.getFonteJogo(15f));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnSim.setContentAreaFilled(false);
        btnSim.setBorderPainted(false);
        btnSim.setFocusPainted(false);
        btnSim.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSim.setBounds(50, 180, 140, 45);
        btnSim.addActionListener(e -> {
            resposta[0] = true;
            dialog.dispose();
        });
        painel.add(btnSim);

        JButton btnNao = new JButton("NÃO") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(100, 60, 200) : new Color(70, 40, 150));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(160, 130, 255, 180));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(Color.WHITE);
                g2.setFont(JogoAudrey.getFonteJogo(15f));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnNao.setContentAreaFilled(false);
        btnNao.setBorderPainted(false);
        btnNao.setFocusPainted(false);
        btnNao.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNao.setBounds(230, 180, 140, 45);
        btnNao.addActionListener(e -> dialog.dispose());
        painel.add(btnNao);

        dialog.setContentPane(painel);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.getRootPane().setOpaque(false);
        dialog.setVisible(true);

        return resposta[0];
    }

    public void mostrarMensagem(String titulo, String mensagem) {
        JDialog dialog = new JDialog(this, titulo, true);
        dialog.setUndecorated(true);
        dialog.setSize(430, 300);
        dialog.setLocationRelativeTo(this);

        JPanel painel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                GradientPaint bg = new GradientPaint(0, 0, new Color(28, 16, 62), w, h, new Color(48, 26, 92));
                g2.setPaint(bg);
                g2.fillRoundRect(0, 0, w, h, 30, 30);
                g2.setColor(new Color(130, 90, 230, 210));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 2, h - 2, 30, 30);

                RadialGradientPaint brilho = new RadialGradientPaint(w / 2f, 70, 180,
                        new float[]{0f, 1f},
                        new Color[]{new Color(160, 120, 255, 90), new Color(255, 255, 255, 0)});
                g2.setPaint(brilho);
                g2.fillRoundRect(0, 0, w, h, 30, 30);

                g2.setColor(new Color(170, 140, 255, 70));
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(-30, -30, 90, 90, 0, 90);
                g2.drawArc(w - 60, h - 60, 90, 90, 180, 90);

                g2.setColor(new Color(90, 60, 170, 120));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(30, 92, w - 30, 92);
                g2.dispose();
            }
        };
        painel.setOpaque(false);

        boolean sucesso = "Sucesso".equalsIgnoreCase(titulo);
        Color corIcone = sucesso ? new Color(60, 180, 110) : new Color(120, 80, 220);

        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int s = getWidth();
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillOval(-4, -4, s + 8, s + 8);
                GradientPaint grad = new GradientPaint(0, 0, corIcone.brighter(), s, s, corIcone.darker());
                g2.setPaint(grad);
                g2.fillOval(0, 0, s, s);
                g2.setColor(new Color(255, 255, 255, 200));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(1, 1, s - 2, s - 2);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (sucesso) {
                    g2.drawPolyline(new int[]{s / 4, s / 2, 3 * s / 4}, new int[]{s / 2, 2 * s / 3, s / 3}, 3);
                } else {
                    g2.setFont(getFonteJogo(26f));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("i", (s - fm.stringWidth("i")) / 2,
                            (s + fm.getAscent() - fm.getDescent()) / 2);
                }
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setBounds(180, 22, 70, 70);
        painel.add(badge);

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(getFonteJogo(24f));
        lblTitulo.setForeground(new Color(225, 205, 255));
        lblTitulo.setBounds(0, 96, 430, 38);
        painel.add(lblTitulo);

        String msgHtml = "<html><div style='text-align:center;line-height:1.4'>" + mensagem.replace("\n", "<br>") + "</div></html>";
        JLabel lblPerg = new JLabel(msgHtml, SwingConstants.CENTER);
        lblPerg.setFont(getFonteJogo(17f));
        lblPerg.setForeground(new Color(190, 170, 235));
        lblPerg.setBounds(30, 135, 370, 70);
        painel.add(lblPerg);

        JButton btnOk = new JButton("OK") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = getModel().isRollover();
                GradientPaint grad = new GradientPaint(0, 0,
                        hover ? new Color(120, 80, 220) : new Color(90, 55, 180),
                        0, getHeight(), hover ? new Color(90, 55, 180) : new Color(70, 40, 150));
                g2.setPaint(grad);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(180, 150, 255, 180));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(Color.WHITE);
                g2.setFont(getFonteJogo(16f));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnOk.setContentAreaFilled(false);
        btnOk.setBorderPainted(false);
        btnOk.setFocusPainted(false);
        btnOk.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnOk.setBounds(140, 215, 150, 48);
        btnOk.addActionListener(e -> dialog.dispose());
        painel.add(btnOk);

        dialog.setContentPane(painel);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.getRootPane().setOpaque(false);
        dialog.setVisible(true);
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
        garantirImagemExiste("ginasio.png");
        garantirImagemExiste("chave.png");
        garantirImagemExiste("livro.png");
        garantirImagemExiste("cutscene_sala_1.png");
        garantirImagemExiste("cutscene_sala_2.png");
        garantirImagemExiste("cutscene_final_1.png");
        garantirImagemExiste("cutscene_final_2.png");
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
        setBackground(new Color(45, 35, 95));
        carregarFonts();
        timerLoad = new Timer(20, this);
    }

    public void iniciarCarregamento() {
        modoContinuar = false;
        continuarProps = null;
        progresso = 0;
        anguloAnimacao = 0;
        GerenciadorAudio.tocarMusicaCarregamento();
        timerLoad.restart();
    }

    public void iniciarCarregamentoContinuar(java.util.Properties props) {
        this.modoContinuar = true;
        this.continuarProps = props;
        progresso = 0;
        anguloAnimacao = 0;
        GerenciadorAudio.tocarMusicaCarregamento();
        timerLoad.restart();
    }

    private void carregarFonts() {
        try {
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File(JogoAudrey.resolvePath("CrayonHandRegular2016Demo.ttf")))
                    .deriveFont(28f);
            fontTitulo = Font.createFont(Font.TRUETYPE_FONT, new java.io.File(JogoAudrey.resolvePath("KGSecondChancesSketch.ttf")))
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
            GerenciadorAudio.pararMusicaCarregamento();
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

        // Fundo base igual ao MenuPrincipal
        GradientPaint gradient = new GradientPaint(0, 0, new Color(45, 35, 95), LARGURA, ALTURA, new Color(35, 25, 80));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, LARGURA, ALTURA);

        Image banner = frame.getMenuPrincipal().getImgBanner();
        if (banner != null) {
            g2d.drawImage(banner, 0, 0, LARGURA, ALTURA, this);
        }

        // Overlay escura superior para contraste
        GradientPaint overlay = new GradientPaint(0, 0, new Color(0, 0, 0, 180), 0, ALTURA / 2f, new Color(0, 0, 0, 0));
        g2d.setPaint(overlay);
        g2d.fillRect(0, 0, LARGURA, ALTURA / 2);

        // Estrelas animadas de fundo
        for (int i = 0; i < 60; i++) {
            float cx2 = (float) ((Math.sin(i * 1.7 + anguloAnimacao * 0.15) + 1.0) * 500);
            float cy2 = (float) ((Math.cos(i * 2.3 + anguloAnimacao * 0.1) + 1.0) * 375);
            int sz = (i % 3 == 0) ? 3 : 2;
            int alpha = 60 + (i % 4) * 30;
            g2d.setColor(new Color(150, 130, 230, Math.min(alpha, 180))); // Roxo pastel (menu principal)
            g2d.fillOval((int) cx2, (int) cy2, sz, sz);
        }

        // Brilho central suave
        RadialGradientPaint glow = new RadialGradientPaint(
                LARGURA / 2f, ALTURA / 2f, 350,
                new float[]{0f, 1f},
                new Color[]{new Color(100, 80, 180, 80), new Color(255, 255, 255, 0)}
        );
        g2d.setPaint(glow);
        g2d.fillRect(0, 0, LARGURA, ALTURA);

        // Titulo
        g2d.setFont(fontTitulo);
        String titulo = "HOBBY QUEST";
        FontMetrics fmT = g2d.getFontMetrics();
        int tx = (LARGURA - fmT.stringWidth(titulo)) / 2;

        // Sombra do titulo
        g2d.setColor(new Color(30, 20, 70, 150));
        g2d.drawString(titulo, tx + 5, 255);

        // Titulo com gradiente (cores do menu)
        GradientPaint titleGrad = new GradientPaint(
                tx, 180, new Color(75, 65, 135),
                tx + fmT.stringWidth(titulo), 250, new Color(140, 120, 200)
        );
        g2d.setPaint(titleGrad);
        g2d.drawString(titulo, tx, 250);

        // Subtitulo
        g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 22f));
        g2d.setColor(new Color(200, 190, 255)); // Lavanda clara (paleta do menu)
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
        g2d.setColor(new Color(100, 120, 255)); // Borda roxa (menu principal)
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(barX, barY, barW, barH, 14, 14);

        // Preenchimento da barra
        int barFill = (int) (barW * progresso / 100.0);
        if (barFill > 0) {
            GradientPaint barGrad = new GradientPaint(
                    barX, barY, new Color(75, 65, 135), // Roxo escuro do menu
                    barX + barFill, barY, new Color(160, 140, 255) // Roxo claro
            );
            g2d.setPaint(barGrad);
            g2d.fillRoundRect(barX, barY, barFill, barH, 14, 14);
            // Reflexo brilhante na barra
            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.fillRoundRect(barX + 2, barY + 2, barFill - 4, barH / 2 - 2, 10, 10);
        }

        // Porcentagem
        g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 20f));
        g2d.setColor(new Color(200, 190, 255)); // Roxo claro (pra leitura)
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
            g2d.setColor(new Color(160, 140, 255, (int) (alpha2 * 255))); // Roxo pastel
            int x1 = spinX + (int) (Math.cos(angle) * (r - 6));
            int y1 = spinY + (int) (Math.sin(angle) * (r - 6));
            int x2 = spinX + (int) (Math.cos(angle) * r);
            int y2 = spinY + (int) (Math.sin(angle) * r);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Texto "Carregando..."
        g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 16f));
        g2d.setColor(new Color(180, 170, 230));
        String loading = "Carregando...";
        FontMetrics fmL = g2d.getFontMetrics();
        g2d.drawString(loading, (LARGURA - fmL.stringWidth(loading)) / 2, spinY + r + 30);

        // Creditos rodape
        g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 13f));
        g2d.setColor(new Color(160, 140, 230));
        String credito = "Desenvolvido com Java Swing";
        FontMetrics fmC = g2d.getFontMetrics();
        g2d.drawString(credito, (LARGURA - fmC.stringWidth(credito)) / 2, ALTURA - 25);
    }
}

class MenuPrincipal extends JPanel implements ActionListener {

    private JogoAudrey frame;
    private JButton btnPlay, btnContinuar, btnConfig, btnSobre, btnSair;
    private JButton btnSuporte, btnPerfil;
    private Font fontCrayonHand, fontTitulo;
    private Color corPrincipal;
    private float animAngulo = 0;
    private Timer animTimer;
    private Image imgBanner;
    private Image imgLogo;

    public Image getImgBanner() {
        return imgBanner;
    }

    public MenuPrincipal(JogoAudrey frame) {
        this.frame = frame;
        corPrincipal = new Color(45, 35, 95);
        setBackground(new Color(255, 245, 235));
        setLayout(null);

        carregarFonts();
        criarComponentes();
        animTimer = new Timer(30, this);
        animTimer.start();
        try {
            imgBanner = new ImageIcon(JogoAudrey.resolvePath("banner_menu.jpg")).getImage();
        } catch (Exception ex) {
            imgBanner = null;
        }
        try {
            imgLogo = new ImageIcon(JogoAudrey.resolvePath("titulo.png")).getImage();
        } catch (Exception ex) {
            imgLogo = null;
        }
    }

    private void carregarFonts() {
        try {
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File(JogoAudrey.resolvePath("CrayonHandRegular2016Demo.ttf")))
                    .deriveFont(24f);
            fontTitulo = Font.createFont(Font.TRUETYPE_FONT, new java.io.File(JogoAudrey.resolvePath("KGSecondChancesSketch.ttf")))
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

        btnConfig = criarBotao("CONFIGURAÇÕES");
        btnConfig.addActionListener(e -> frame.mostrarConfiguracoes("menuPrincipal"));
        add(btnConfig);

        btnSobre = criarBotao("SOBRE O JOGO");
        btnSobre.addActionListener(e -> mostrarSobre());
        add(btnSobre);

        btnSair = criarBotao("SAIR");
        btnSair.addActionListener(e -> frame.confirmarSaida());
        add(btnSair);

        // Botao de suporte (?)
        btnSuporte = new JButton("?") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = getModel().isRollover();
                g2.setColor(hover ? new Color(100, 60, 180, 220) : new Color(60, 40, 120, 200));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(200, 180, 255));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(1, 1, getWidth() - 2, getHeight() - 2);
                g2.setColor(Color.WHITE);
                g2.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 20f));
                FontMetrics fm2 = g2.getFontMetrics();
                g2.drawString("?", (getWidth() - fm2.stringWidth("?")) / 2,
                        (getHeight() + fm2.getAscent() - fm2.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnSuporte.setContentAreaFilled(false);
        btnSuporte.setBorderPainted(false);
        btnSuporte.setFocusPainted(false);
        btnSuporte.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSuporte.setToolTipText("Suporte / Ajuda");
        btnSuporte.addActionListener(e -> mostrarSuporte());
        add(btnSuporte);

        // Botao de perfil (icone de usuario circular)
        btnPerfil = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = getModel().isRollover();
                g2.setColor(hover ? new Color(100, 60, 180, 220) : new Color(60, 40, 120, 200));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(200, 180, 255));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.fillOval(cx - 8, cy - 13, 16, 16);
                g2.fillArc(cx - 12, cy + 2, 24, 18, 0, 180);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(1, 1, getWidth() - 2, getHeight() - 2);
                g2.dispose();
            }
        };
        btnPerfil.setContentAreaFilled(false);
        btnPerfil.setBorderPainted(false);
        btnPerfil.setFocusPainted(false);
        btnPerfil.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnPerfil.setToolTipText("Minha Conta");
        btnPerfil.addActionListener(e -> mostrarPerfil());
        add(btnPerfil);

        atualizarBotoes();
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int w = getWidth();
        int h = getHeight();
        int bw = Math.max(240, w / 4);
        int bh = Math.max(55, h / 12);
        int bx = (int) (w * 0.05); // alinhado a esquerda
        int startY = (int) (h * 0.35); // subiu para cima
        int gap = (int) (h * 0.11); // espaçamento maior entre botoes
        JButton[] btns = {btnPlay, btnContinuar, btnConfig, btnSobre, btnSair};
        for (int i = 0; i < btns.length; i++) {
            if (btns[i] != null) {
                btns[i].setBounds(bx, startY + i * gap, bw, bh);
            }
        }
        // Botoes de canto superior direito
        int btnSize = 44;
        int margin = 16;
        if (btnPerfil != null) {
            btnPerfil.setBounds(w - margin - btnSize, margin, btnSize, btnSize);
        }
        if (btnSuporte != null) {
            btnSuporte.setBounds(w - margin - btnSize * 2 - 10, margin, btnSize, btnSize);
        }
    }

    public void atualizarBotoes() {
        if (btnContinuar != null) {
            boolean existe = Database.saveExiste(1) || Database.saveExiste(2) || Database.saveExiste(3);
            btnContinuar.setEnabled(existe);
            btnContinuar.setBackground(existe ? corPrincipal : new Color(30, 20, 50));
            btnContinuar.setForeground(existe ? new Color(255, 255, 255) : new Color(100, 100, 120));
        }
    }

    private JButton criarBotao(String texto) {
        return new BotaoEstilizado(texto, fontCrayonHand);
    }

    private void mostrarSobre() {
        String mensagem = "Robbie Quest\n\n"
                + "Um jogo de aventura onde você ajuda Audrey\n"
                + "a resolver misterios e coletar itens especiais.\n\n"
                + "CONTROLES:\n"
                + "A - Mover para esquerda\n"
                + "D - Mover para direita\n"
                + "E - Interagir com objetos e pegar itens\n"
                + "F - Falar com personagens\n"
                + "Q - Fechar dialogos e armário\n"
                + "B - Abrir/Fechar inventario\n"
                + "M - Mostrar/Esconder objetivos\n"
                + "ESC - Abrir menu em jogo\n\n"
                + "Desenvolvido com Java Swing";

        JOptionPane.showMessageDialog(
                this,
                mensagem,
                "Sobre o Jogo",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarSuporte() {
        JDialog dialog = new JDialog(frame, "Suporte", true);
        dialog.setUndecorated(true);
        dialog.setSize(420, 400);
        dialog.setLocationRelativeTo(frame);

        JPanel painel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                GradientPaint bg = new GradientPaint(0, 0, new Color(25, 15, 55), w, h, new Color(40, 20, 80));
                g2.setPaint(bg);
                g2.fillRoundRect(0, 0, w, h, 30, 30);
                g2.setColor(new Color(120, 80, 220, 200));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 2, h - 2, 30, 30);
                g2.setColor(new Color(100, 70, 180, 100));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(30, 115, w - 30, 115);
                g2.dispose();
            }
        };
        painel.setOpaque(false);

        // Icone ? no topo
        JPanel icone = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int sz = getWidth();
                g2.setColor(new Color(120, 80, 220, 80));
                g2.fillOval(-6, -6, sz + 12, sz + 12);
                GradientPaint av = new GradientPaint(0, 0, new Color(80, 50, 160), sz, sz, new Color(50, 30, 110));
                g2.setPaint(av);
                g2.fillOval(0, 0, sz, sz);
                g2.setColor(Color.WHITE);
                g2.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 36f));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("?", (sz - fm.stringWidth("?")) / 2, (sz + fm.getAscent() - fm.getDescent()) / 2);
                g2.setColor(new Color(160, 120, 255));
                g2.setStroke(new BasicStroke(3f));
                g2.drawOval(2, 2, sz - 4, sz - 4);
                g2.dispose();
            }
        };
        icone.setOpaque(false);
        icone.setBounds(160, 20, 100, 100);
        painel.add(icone);

        // Titulo
        JLabel lblTitulo = new JLabel("Suporte / Ajuda", SwingConstants.CENTER);
        lblTitulo.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 20f));
        lblTitulo.setForeground(new Color(220, 200, 255));
        lblTitulo.setBounds(0, 125, 420, 28);
        painel.add(lblTitulo);

        // Linhas de info
        String[][] infos = {
            {"Encontrou um problema?", new Color(170, 150, 255).toString()},};

        JLabel lblDesc = new JLabel("<html><div style='text-align:center;line-height:1.6'>"
                + "Encontrou um problema ou tem dúvidas?<br>"
                + "Entre em contato com o suporte:</div></html>", SwingConstants.CENTER);
        lblDesc.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 14f));
        lblDesc.setForeground(new Color(170, 150, 220));
        lblDesc.setBounds(20, 160, 380, 50);
        painel.add(lblDesc);

        // Card de email
        JPanel cardEmail = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60, 40, 120, 180));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(100, 80, 200, 150));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        cardEmail.setOpaque(false);
        cardEmail.setLayout(null);
        cardEmail.setBounds(30, 220, 360, 50);

        JLabel lblEmailIcon = new JLabel("✉  suporte@robbiequest.com", SwingConstants.CENTER);
        lblEmailIcon.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 15f));
        lblEmailIcon.setForeground(new Color(200, 180, 255));
        lblEmailIcon.setBounds(0, 0, 360, 50);
        cardEmail.add(lblEmailIcon);
        painel.add(cardEmail);

        JLabel lblDica = new JLabel("<html><div style='text-align:center'>"
                + "Ao reportar um bug, descreva o que estava fazendo.</div></html>",
                SwingConstants.CENTER);
        lblDica.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 12f));
        lblDica.setForeground(new Color(120, 100, 160));
        lblDica.setBounds(20, 278, 380, 30);
        painel.add(lblDica);

        JLabel lblVersao = new JLabel("Robbie Quest v1.0.0", SwingConstants.CENTER);
        lblVersao.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 12f));
        lblVersao.setForeground(new Color(100, 90, 140));
        lblVersao.setBounds(0, 313, 420, 18);
        painel.add(lblVersao);

        // Botao fechar
        JButton btnFechar = new JButton("FECHAR") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = getModel().isRollover();
                g2.setColor(hover ? new Color(100, 60, 200) : new Color(70, 40, 150));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(160, 130, 255, 180));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(Color.WHITE);
                g2.setFont(JogoAudrey.getFonteJogo(15f));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("FECHAR", (getWidth() - fm.stringWidth("FECHAR")) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnFechar.setContentAreaFilled(false);
        btnFechar.setBorderPainted(false);
        btnFechar.setFocusPainted(false);
        btnFechar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnFechar.setBounds(140, 340, 140, 42);
        btnFechar.addActionListener(e -> dialog.dispose());
        painel.add(btnFechar);

        dialog.setContentPane(painel);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.getRootPane().setOpaque(false);
        dialog.setVisible(true);
    }

    private void mostrarPerfil() {
        String usuario = System.getProperty("user.name", "Jogador");
        boolean slot1 = Database.saveExiste(1);
        boolean slot2 = Database.saveExiste(2);
        boolean slot3 = Database.saveExiste(3);

        JDialog dialog = new JDialog(frame, "Minha Conta", true);
        dialog.setUndecorated(true);
        dialog.setSize(400, 420);
        dialog.setLocationRelativeTo(frame);

        JPanel painel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                // Fundo gradiente roxo escuro
                GradientPaint bg = new GradientPaint(0, 0, new Color(25, 15, 55), w, h, new Color(40, 20, 80));
                g2.setPaint(bg);
                g2.fillRoundRect(0, 0, w, h, 30, 30);
                // Borda brilhante
                g2.setColor(new Color(120, 80, 220, 200));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 2, h - 2, 30, 30);
                // Linha divisória abaixo do avatar
                g2.setColor(new Color(100, 70, 180, 100));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(30, 195, w - 30, 195);
                g2.dispose();
            }
        };
        painel.setOpaque(false);

        // Avatar circular
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int sz = getWidth();
                // Glow externo
                g2.setColor(new Color(120, 80, 220, 80));
                g2.fillOval(-6, -6, sz + 12, sz + 12);
                // Fundo do avatar
                GradientPaint av = new GradientPaint(0, 0, new Color(80, 50, 160), sz, sz, new Color(50, 30, 110));
                g2.setPaint(av);
                g2.fillOval(0, 0, sz, sz);
                // Icone de usuario
                g2.setColor(new Color(200, 180, 255));
                int cx = sz / 2, cy = sz / 2;
                g2.fillOval(cx - 18, cy - 25, 36, 36); // cabeca
                g2.fillArc(cx - 26, cy + 10, 52, 38, 0, 180); // corpo
                // Borda
                g2.setColor(new Color(160, 120, 255));
                g2.setStroke(new BasicStroke(3f));
                g2.drawOval(2, 2, sz - 4, sz - 4);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setBounds(150, 30, 100, 100);
        painel.add(avatar);

        // Nome do usuario
        JLabel lblNome = new JLabel(usuario, SwingConstants.CENTER);
        lblNome.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 22f));
        lblNome.setForeground(new Color(220, 200, 255));
        lblNome.setBounds(0, 140, 400, 30);
        painel.add(lblNome);

        JLabel lblSubtitulo = new JLabel("Robbie Quest — Jogador", SwingConstants.CENTER);
        lblSubtitulo.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 14f));
        lblSubtitulo.setForeground(new Color(150, 130, 200));
        lblSubtitulo.setBounds(0, 170, 400, 20);
        painel.add(lblSubtitulo);

        // Secao de saves
        JLabel lblSaveTitulo = new JLabel("SAVES", SwingConstants.CENTER);
        lblSaveTitulo.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 15f));
        lblSaveTitulo.setForeground(new Color(180, 150, 255));
        lblSaveTitulo.setBounds(0, 210, 400, 24);
        painel.add(lblSaveTitulo);

        String[] slots = {"Slot 1", "Slot 2", "Slot 3"};
        boolean[] ativos = {slot1, slot2, slot3};
        for (int i = 0; i < 3; i++) {
            final boolean ativo = ativos[i];
            JPanel slotPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(ativo ? new Color(70, 50, 140, 180) : new Color(30, 20, 60, 120));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    if (ativo) {
                        g2.setColor(new Color(100, 80, 200, 150));
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    }
                    g2.dispose();
                }
            };
            slotPanel.setOpaque(false);
            slotPanel.setLayout(null);
            slotPanel.setBounds(30 + i * 115, 244, 105, 50);

            JLabel slotNome = new JLabel(slots[i], SwingConstants.CENTER);
            slotNome.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 14f));
            slotNome.setForeground(ativo ? new Color(200, 180, 255) : new Color(100, 90, 140));
            slotNome.setBounds(0, 4, 105, 20);
            slotPanel.add(slotNome);

            JLabel slotStatus = new JLabel(ativo ? "● Usado" : "○ Vazio", SwingConstants.CENTER);
            slotStatus.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 12f));
            slotStatus.setForeground(ativo ? new Color(160, 255, 160) : new Color(100, 90, 130));
            slotStatus.setBounds(0, 26, 105, 16);
            slotPanel.add(slotStatus);

            painel.add(slotPanel);
        }

        // Versao
        JLabel lblVersao = new JLabel("Robbie Quest v1.0.0", SwingConstants.CENTER);
        lblVersao.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 12f));
        lblVersao.setForeground(new Color(100, 90, 140));
        lblVersao.setBounds(0, 310, 400, 20);
        painel.add(lblVersao);

        // Botao fechar estilizado
        JButton btnFechar = new JButton("FECHAR") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = getModel().isRollover();
                g2.setColor(hover ? new Color(100, 60, 200) : new Color(70, 40, 150));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(160, 130, 255, 180));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(Color.WHITE);
                g2.setFont(JogoAudrey.getFonteJogo(15f));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("FECHAR", (getWidth() - fm.stringWidth("FECHAR")) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnFechar.setContentAreaFilled(false);
        btnFechar.setBorderPainted(false);
        btnFechar.setFocusPainted(false);
        btnFechar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnFechar.setBounds(130, 345, 140, 42);
        btnFechar.addActionListener(e -> dialog.dispose());
        painel.add(btnFechar);

        dialog.setContentPane(painel);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.getRootPane().setOpaque(false);
        dialog.setVisible(true);
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

        // Fundo base (gradiente escuro) para preencher espacos vazios
        GradientPaint gradient = new GradientPaint(0, 0, new Color(30, 20, 50), W, H, new Color(20, 10, 40));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, W, H);

        // Imagem de fundo completa em tela cheia (preenche a tela toda sem cortar as
        // bordas)
        if (imgBanner != null) {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.drawImage(imgBanner, 0, 0, W, H, this);
        }

        // Overlay escuro semitransparente para legibilidade
        g2d.setColor(new Color(10, 5, 25, 140));
        g2d.fillRect(0, 0, W, H);

        // Desenhos decorativos suaves (cantos lavanda + partículas roxas)
        desenharCantosDecorativos(g2d, W, H);
        desenharParticulasFundo(g2d, W, H);

        // Brilho suave atras dos botoes
        int glowCx = (int) (W * 0.20), glowCy = (int) (H * 0.62);
        int glowR = (int) (Math.max(W, H) * 0.50);
        RadialGradientPaint glow = new RadialGradientPaint(
                glowCx, glowCy, glowR,
                new float[]{0f, 1f},
                new Color[]{new Color(140, 100, 235, 45), new Color(140, 100, 235, 0)});
        g2d.setPaint(glow);
        g2d.fillRect(0, 0, W, H);

        // Banner decorativo atras do titulo (substituido pela Logo)
        int titleY = (int) (H * 0.20);
        if (imgLogo != null) {
            int logoW = imgLogo.getWidth(null);
            int logoH = imgLogo.getHeight(null);
            if (logoW > 0 && logoH > 0) {
                int maxWidth = (int) (W * 0.50); // limita a metade esquerda da tela
                int maxHeight = (int) (H * 0.55);
                double scale = Math.min((double) maxWidth / logoW, (double) maxHeight / logoH);
                logoW = (int) (logoW * scale);
                logoH = (int) (logoH * scale);
                // Centraliza a logo sobre os botoes (mesmo centro que os botoes)
                int btnW = Math.max(240, W / 4);
                int btnX = (int) (W * 0.05);
                int logoCentro = btnX + btnW / 2;
                int logoX = logoCentro - logoW / 2;
                int logoY = 5; // bem no topo
                g2d.drawImage(imgLogo, logoX, logoY, logoW, logoH, this);
            }
        } else {
            // Fallback caso a imagem não exista
            g2d.setFont(fontTitulo);
            String titulo = "Robbie Quest";
            FontMetrics fm = g2d.getFontMetrics();
            int tx = (W - fm.stringWidth(titulo)) / 2;
            int bannerW = fm.stringWidth(titulo) + 100;
            int tituloBannerH = fm.getHeight() + 40;
            int bannerX = (W - bannerW) / 2;
            int bannerY = titleY - fm.getAscent() - 15;
            g2d.setColor(new Color(20, 10, 50, 200));
            g2d.fillRoundRect(bannerX, bannerY, bannerW, tituloBannerH, 35, 35);
            g2d.setColor(new Color(220, 180, 255));
            g2d.drawString(titulo, tx, titleY);
        }

        // Painel dark glass atras dos botoes
        int btnStartY = (int) (H * 0.44);
        int btnEndY = btnStartY + 5 * (int) (H * 0.09) + 20;
        int cardW = Math.max(300, W / 3);
        int cardX = (W - cardW) / 2;
        int cardH = btnEndY - btnStartY + 40;

        // Fundo transparente removido conforme pedido do usuario
        // Borda removida conforme pedido do usuario
    }

    private void desenharCantosDecorativos(Graphics2D g2d, int w, int h) {
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int marg = 22, tam = 32;
        Color[] cores = {
            new Color(150, 130, 230, 150),
            new Color(200, 190, 255, 150),
            new Color(120, 90, 220, 140),
            new Color(180, 160, 230, 150)
        };

        g2d.setColor(cores[0]);
        g2d.drawArc(marg, marg, tam * 2, tam * 2, 180, 90);
        g2d.fillOval(marg - 3, marg - 3, 6, 6);
        g2d.fillOval(marg + tam - 3, marg + tam - 3, 6, 6);

        g2d.setColor(cores[1]);
        g2d.drawArc(w - marg - tam * 2, marg, tam * 2, tam * 2, 270, 90);
        g2d.fillOval(w - marg - 3, marg - 3, 6, 6);
        g2d.fillOval(w - marg - tam - 3, marg + tam - 3, 6, 6);

        g2d.setColor(cores[2]);
        g2d.drawArc(marg, h - marg - tam * 2, tam * 2, tam * 2, 90, 90);
        g2d.fillOval(marg - 3, h - marg - 3, 6, 6);
        g2d.fillOval(marg + tam - 3, h - marg - tam - 3, 6, 6);

        g2d.setColor(cores[3]);
        g2d.drawArc(w - marg - tam * 2, h - marg - tam * 2, tam * 2, tam * 2, 0, 90);
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
                case 0:
                    cor = new Color(200, 190, 255, Math.min(alpha, 160));
                    break;
                case 1:
                    cor = new Color(150, 130, 230, Math.min(alpha, 150));
                    break;
                case 2:
                    cor = new Color(120, 90, 220, Math.min(alpha, 150));
                    break;
                case 3:
                    cor = new Color(180, 160, 230, Math.min(alpha, 150));
                    break;
                case 4:
                    cor = new Color(235, 210, 255, Math.min(alpha, 140));
                    break;
                default:
                    cor = new Color(160, 140, 220, Math.min(alpha, 140));
            }

            int tipo = i % 5;
            if (tipo == 0) {
                // Estrelinha de 4 pontas
                int r = sz / 2 + 1;
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.setColor(cor);
                g2d.drawLine((int) (cx - r), (int) cy, (int) (cx + r), (int) cy);
                g2d.drawLine((int) cx, (int) (cy - r), (int) cx, (int) (cy + r));
                g2d.drawLine((int) (cx - r / 2), (int) (cy - r / 2), (int) (cx + r / 2), (int) (cy + r / 2));
                g2d.drawLine((int) (cx - r / 2), (int) (cy + r / 2), (int) (cx + r / 2), (int) (cy - r / 2));
            } else if (tipo == 1) {
                // Coracao miniatura
                int hsz = sz;
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.setColor(cor);
                GeneralPath hp = new GeneralPath();
                hp.moveTo(cx, cy + hsz / 3);
                hp.curveTo(cx, cy - hsz / 3, cx - hsz, cy - hsz / 3, cx - hsz, cy);
                hp.curveTo(cx - hsz, cy + hsz * 0.5, cx, cy + hsz, cx, cy + hsz / 3);
                hp.curveTo(cx, cy + hsz, cx + hsz, cy + hsz * 0.5, cx + hsz, cy);
                hp.curveTo(cx + hsz, cy - hsz / 3, cx, cy - hsz / 3, cx, cy + hsz / 3);
                g2d.draw(hp);
            } else {
                g2d.setColor(cor);
                g2d.fillOval((int) cx, (int) cy, sz, sz);
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
        mEstrela4(g2d, new Color(255, 215, 100, 170), (int) (w * 0.88 + Math.sin(a * 0.7) * 5),
                (int) (h * 0.10 + Math.cos(a * 0.8) * 5), 16);
        // Nuvem (esquerda meio)
        mNuvem(g2d, new Color(170, 205, 255, 150), (int) (w * 0.05 + Math.sin(a * 0.5) * 3),
                (int) (h * 0.42 + Math.cos(a * 0.6) * 4));
        // Coracao (direita meio) - pulsando
        float pulse = 1.0f + (float) Math.sin(a * 1.2) * 0.15f;
        mCoracao(g2d, new Color(255, 155, 190, 160), (int) (w * 0.87 + Math.sin(a * 0.9) * 4),
                (int) (h * 0.50 + Math.cos(a) * 5), (int) (22 * pulse));
        // Laco (baixo esquerdo)
        mLaco(g2d, new Color(255, 185, 210, 160), (int) (w * 0.08 + Math.sin(a * 0.6) * 3),
                (int) (h * 0.77 + Math.cos(a * 0.7) * 4));
        // Estrela 5pts (baixo direito)
        mEstrela5(g2d, new Color(255, 200, 110, 165), (int) (w * 0.89 + Math.sin(a * 0.8) * 4),
                (int) (h * 0.80 + Math.cos(a * 0.9) * 5), 20);
        // Espiral (baixo centro-direita)
        mEspiral(g2d, new Color(180, 195, 255, 155), (int) (w * 0.80 + Math.sin(a * 0.4) * 3),
                (int) (h * 0.20 + Math.cos(a * 0.5) * 4));

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
        g2d.drawOval(borX - asaW / 2, borY, asaW / 2, asaH / 2);
        g2d.drawOval(borX + asaW / 4, borY, asaW / 2, asaH / 2);
        g2d.setColor(new Color(255, 180, 200, 180));
        g2d.fillOval(borX - 2, borY - 2, 4, 4);
        // Antenas
        g2d.drawLine(borX - 2, borY - 3, borX - 6, borY - 10);
        g2d.drawLine(borX + 2, borY - 3, borX + 6, borY - 10);

        // --- Icones de hobbies ---
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
        g2d.fillRoundRect(halX - barW / 2, halY - barH / 2, barW, barH, 2, 2);
        g2d.fillRoundRect(halX - barW / 2 - pesoW + 1, halY - pesoH / 2, pesoW, pesoH, 2, 2);
        g2d.fillRoundRect(halX + barW / 2 - 1, halY - pesoH / 2, pesoW, pesoH, 2, 2);
        g2d.setColor(new Color(120, 120, 140, 180));
        g2d.drawRoundRect(halX - barW / 2, halY - barH / 2, barW, barH, 2, 2);
        g2d.drawRoundRect(halX - barW / 2 - pesoW + 1, halY - pesoH / 2, pesoW, pesoH, 2, 2);
        g2d.drawRoundRect(halX + barW / 2 - 1, halY - pesoH / 2, pesoW, pesoH, 2, 2);

        // Bolinhas decorativas pulsando com gradiente de cores
        for (int i = 0; i < 5; i++) {
            float by = (float) (h * 0.88 + Math.sin(a + i * 1.1) * 5);
            float bx = (float) (w * 0.25 + i * 25);
            float bs = 8 + (float) Math.sin(a * 1.5 + i) * 1.5f;
            Color bc;
            switch (i % 4) {
                case 0:
                    bc = new Color(255, 200, 220, 130);
                    break;
                case 1:
                    bc = new Color(200, 230, 200, 130);
                    break;
                case 2:
                    bc = new Color(200, 220, 255, 130);
                    break;
                default:
                    bc = new Color(255, 240, 180, 130);
                    break;
            }
            g2d.setColor(bc);
            g2d.fillOval((int) bx, (int) (by - bs / 2), (int) bs, (int) bs);
        }
        for (int i = 0; i < 4; i++) {
            float by = (float) (h * 0.84 + Math.cos(a + i * 1.3) * 5);
            float bx = (float) (w * 0.62 + i * 25);
            float bs = 9 + (float) Math.cos(a * 1.3 + i * 0.7) * 2;
            Color bc;
            switch (i % 4) {
                case 0:
                    bc = new Color(200, 230, 200, 120);
                    break;
                case 1:
                    bc = new Color(255, 200, 220, 120);
                    break;
                case 2:
                    bc = new Color(255, 240, 180, 120);
                    break;
                default:
                    bc = new Color(200, 220, 255, 120);
                    break;
            }
            g2d.setColor(bc);
            g2d.fillOval((int) bx, (int) (by - bs / 2), (int) bs, (int) bs);
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
            if (i == 0) {
                star.moveTo(px, py);
            } else {
                star.lineTo(px, py);
            }
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
            if (rad < 0) {
                break;
            }
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
        corPrincipal = new Color(45, 35, 95); // Cor do menu principal
        setBackground(new Color(30, 20, 50)); // Fundo escuro do menu principal
        setLayout(null);

        carregarFonts();
        criarComponentes();
        animTimer = new Timer(30, this);
        animTimer.start();
    }

    private void carregarFonts() {
        try {
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File(JogoAudrey.resolvePath("CrayonHandRegular2016Demo.ttf")))
                    .deriveFont(24f);
            fontTitulo = Font.createFont(Font.TRUETYPE_FONT, new java.io.File(JogoAudrey.resolvePath("KGSecondChancesSketch.ttf")))
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
                frame.mostrarMensagem("Sucesso", "Jogo salvo com sucesso no Slot " + slot + "!");
            } else {
                frame.mostrarMenuSlots(JogoAudrey.ACAO_SALVAR);
            }
        });
        add(btnSalvar);

        btnConfig = criarBotao("CONFIGURAÇÕES");
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
        JButton[] btns = {btnContinuar, btnSalvar, btnConfig, btnSobre, btnMenuPrincipal, btnSair};
        for (int i = 0; i < btns.length; i++) {
            if (btns[i] != null) {
                btns[i].setBounds(bx, startY + i * gap, bw, bh);
            }
        }
    }

    private JButton criarBotao(String texto) {
        return new BotaoEstilizado(texto, fontCrayonHand);
    }

    private void mostrarSobre() {
        String mensagem = "Robbie Quest\n\n"
                + "Um jogo de aventura onde você ajuda Audrey\n"
                + "a resolver misterios e coletar itens especiais.\n\n"
                + "CONTROLES:\n"
                + "A - Mover para esquerda\n"
                + "D - Mover para direita\n"
                + "E - Interagir com objetos e pegar itens\n"
                + "F - Falar com personagens\n"
                + "Q - Fechar dialogos e armário\n"
                + "B - Abrir/Fechar inventario\n"
                + "M - Mostrar/Esconder objetivos\n"
                + "ESC - Abrir menu em jogo\n\n"
                + "Desenvolvido com Java Swing";

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

        // Fundo base igual ao MenuPrincipal
        GradientPaint gradient = new GradientPaint(0, 0, new Color(30, 20, 50), W, H, new Color(20, 10, 40));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, W, H);

        Image banner = frame.getMenuPrincipal().getImgBanner();
        if (banner != null) {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.drawImage(banner, 0, 0, W, H, this);
        }

        // Overlay escuro semitransparente para legibilidade
        g2d.setColor(new Color(10, 5, 25, 140));
        g2d.fillRect(0, 0, W, H);

        desenharCantosDecorativos(g2d, W, H);
        desenharParticulasFlutuantes(g2d, W, H);
        desenharIconesHobbiesMenuPausa(g2d, W, H);

        // Banner decorativo atras do titulo PAUSADO
        g2d.setFont(fontTitulo);
        String titulo = "PAUSADO";
        FontMetrics fm = g2d.getFontMetrics();
        int tx = (W - fm.stringWidth(titulo)) / 2;
        int titleY = (int) (H * 0.22);
        int bannerW = fm.stringWidth(titulo) + 100;
        int bannerH = fm.getHeight() + 40;
        int bannerX = (W - bannerW) / 2;
        int bannerY = titleY - fm.getAscent() - 15;

        g2d.setColor(new Color(60, 40, 120, 60));
        g2d.fillRoundRect(bannerX + 5, bannerY + 5, bannerW, bannerH, 35, 35);
        GradientPaint bannerGrad = new GradientPaint(bannerX, bannerY, new Color(255, 255, 245, 240), bannerX,
                bannerY + bannerH, new Color(240, 245, 255, 240));
        g2d.setPaint(bannerGrad);
        g2d.fillRoundRect(bannerX, bannerY, bannerW, bannerH, 35, 35);
        g2d.setColor(new Color(100, 80, 200, 160));
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawRoundRect(bannerX, bannerY, bannerW, bannerH, 35, 35);

        // Glow externo do titulo
        g2d.setFont(JogoAudrey.getCachedFont(fontTitulo, Font.BOLD, 62f));
        fm = g2d.getFontMetrics();
        tx = (W - fm.stringWidth(titulo)) / 2;

        g2d.setColor(new Color(120, 80, 220, 40));
        g2d.drawString(titulo, tx + 8, titleY + 8);
        g2d.drawString(titulo, tx - 8, titleY - 8);
        g2d.setColor(new Color(45, 35, 95, 60));
        g2d.drawString(titulo, tx + 6, titleY - 6);
        g2d.drawString(titulo, tx - 6, titleY + 6);

        g2d.setColor(new Color(75, 65, 135, 70));
        g2d.drawString(titulo, tx + 5, titleY + 5);
        g2d.drawString(titulo, tx - 5, titleY - 5);

        g2d.setColor(new Color(60, 40, 120, 150));
        g2d.drawString(titulo, tx + 3, titleY + 3);

        GradientPaint titleGrad = new GradientPaint(tx, titleY - 60, new Color(120, 80, 220), tx, titleY + 10,
                new Color(45, 35, 95));
        g2d.setPaint(titleGrad);
        g2d.drawString(titulo, tx, titleY);

        // Estrelas decorativas ao lado do titulo
        float a = animAngulo;
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int starR = 7;
        int esx = tx - 30 + (int) (Math.sin(a * 0.7) * 3);
        int esy = titleY - fm.getAscent() / 2 + (int) (Math.cos(a * 0.8) * 3);
        g2d.setColor(new Color(120, 80, 220, 200));
        g2d.drawLine(esx - starR, esy, esx + starR, esy);
        g2d.drawLine(esx, esy - starR, esx, esy + starR);

        int edx = tx + fm.stringWidth(titulo) + 30 + (int) (Math.sin(a * 0.9 + 1) * 3);
        int edy = titleY - fm.getAscent() / 2 + (int) (Math.cos(a * 1.0 + 1) * 3);
        g2d.setColor(new Color(75, 65, 135, 200));
        g2d.drawLine(edx - starR, edy, edx + starR, edy);
        g2d.drawLine(edx, edy - starR, edx, edy + starR);

        // Painel translucido atras dos botoes
        int btnStartY = (int) (H * 0.32);
        int btnEndY = btnStartY + 6 * (int) (H * 0.10) + 20;
        int cardW = Math.max(320, W / 3);
        int cardX = (W - cardW) / 2;
        int cardH = btnEndY - btnStartY + 40;

        g2d.setColor(new Color(255, 255, 245, 90));
        g2d.fillRoundRect(cardX, btnStartY - 20, cardW, cardH, 35, 35);
        g2d.setColor(new Color(100, 80, 200, 80));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(cardX, btnStartY - 20, cardW, cardH, 35, 35);
    }

    private void desenharCantosDecorativos(Graphics2D g2d, int w, int h) {
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int marg = 22, tam = 32;
        Color[] cores = {
            new Color(150, 130, 230, 150),
            new Color(200, 190, 255, 150),
            new Color(120, 90, 220, 140),
            new Color(180, 160, 230, 150)
        };

        g2d.setColor(cores[0]);
        g2d.drawArc(marg, marg, tam * 2, tam * 2, 180, 90);
        g2d.fillOval(marg - 3, marg - 3, 6, 6);
        g2d.fillOval(marg + tam - 3, marg + tam - 3, 6, 6);

        g2d.setColor(cores[1]);
        g2d.drawArc(w - marg - tam * 2, marg, tam * 2, tam * 2, 270, 90);
        g2d.fillOval(w - marg - 3, marg - 3, 6, 6);
        g2d.fillOval(w - marg - tam - 3, marg + tam - 3, 6, 6);

        g2d.setColor(cores[2]);
        g2d.drawArc(marg, h - marg - tam * 2, tam * 2, tam * 2, 90, 90);
        g2d.fillOval(marg - 3, h - marg - 3, 6, 6);
        g2d.fillOval(marg + tam - 3, h - marg - tam - 3, 6, 6);

        g2d.setColor(cores[3]);
        g2d.drawArc(w - marg - tam * 2, h - marg - tam * 2, tam * 2, tam * 2, 0, 90);
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
                case 0:
                    cor = new Color(200, 190, 255, Math.min(alpha, 160));
                    break;
                case 1:
                    cor = new Color(150, 130, 230, Math.min(alpha, 150));
                    break;
                case 2:
                    cor = new Color(120, 90, 220, Math.min(alpha, 150));
                    break;
                case 3:
                    cor = new Color(180, 160, 230, Math.min(alpha, 150));
                    break;
                case 4:
                    cor = new Color(235, 210, 255, Math.min(alpha, 140));
                    break;
                default:
                    cor = new Color(160, 140, 220, Math.min(alpha, 140));
            }

            int tipo = i % 5;
            if (tipo == 0) {
                int r = sz / 2 + 1;
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.setColor(cor);
                g2d.drawLine((int) (cx - r), (int) cy, (int) (cx + r), (int) cy);
                g2d.drawLine((int) cx, (int) (cy - r), (int) cx, (int) (cy + r));
                g2d.drawLine((int) (cx - r / 2), (int) (cy - r / 2), (int) (cx + r / 2), (int) (cy + r / 2));
                g2d.drawLine((int) (cx - r / 2), (int) (cy + r / 2), (int) (cx + r / 2), (int) (cy - r / 2));
            } else if (tipo == 1) {
                int hsz = sz;
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.setColor(cor);
                GeneralPath hp = new GeneralPath();
                hp.moveTo(cx, cy + hsz / 3);
                hp.curveTo(cx, cy - hsz / 3, cx - hsz, cy - hsz / 3, cx - hsz, cy);
                hp.curveTo(cx - hsz, cy + hsz * 0.5, cx, cy + hsz, cx, cy + hsz / 3);
                hp.curveTo(cx, cy + hsz, cx + hsz, cy + hsz * 0.5, cx + hsz, cy);
                hp.curveTo(cx + hsz, cy - hsz / 3, cx, cy - hsz / 3, cx, cy + hsz / 3);
                g2d.draw(hp);
            } else {
                g2d.setColor(cor);
                g2d.fillOval((int) cx, (int) cy, sz, sz);
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
        g2d.drawOval(borX - asaW / 2, borY, asaW / 2, asaH / 2);
        g2d.drawOval(borX + asaW / 4, borY, asaW / 2, asaH / 2);
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
        g2d.fillRoundRect(halX - barW / 2, halY - barH / 2, barW, barH, 2, 2);
        g2d.fillRoundRect(halX - barW / 2 - pesoW + 1, halY - pesoH / 2, pesoW, pesoH, 2, 2);
        g2d.fillRoundRect(halX + barW / 2 - 1, halY - pesoH / 2, pesoW, pesoH, 2, 2);
        g2d.setColor(new Color(120, 120, 140, 170));
        g2d.drawRoundRect(halX - barW / 2, halY - barH / 2, barW, barH, 2, 2);
        g2d.drawRoundRect(halX - barW / 2 - pesoW + 1, halY - pesoH / 2, pesoW, pesoH, 2, 2);
        g2d.drawRoundRect(halX + barW / 2 - 1, halY - pesoH / 2, pesoW, pesoH, 2, 2);

        // Bolinhas decorativas
        for (int i = 0; i < 4; i++) {
            float by = (float) (h * 0.88 + Math.sin(a + i * 1.1) * 4);
            float bx = (float) (w * 0.20 + i * 22);
            float bs = 8 + (float) Math.sin(a * 1.4 + i) * 1.5f;
            Color bc;
            switch (i % 4) {
                case 0:
                    bc = new Color(255, 200, 220, 120);
                    break;
                case 1:
                    bc = new Color(200, 230, 200, 120);
                    break;
                case 2:
                    bc = new Color(200, 220, 255, 120);
                    break;
                default:
                    bc = new Color(255, 240, 180, 120);
                    break;
            }
            g2d.setColor(bc);
            g2d.fillOval((int) bx, (int) (by - bs / 2), (int) bs, (int) bs);
        }
    }
}

class JogoPanel extends JPanel implements ActionListener, KeyListener, MouseListener {

    private JogoAudrey frame;
    private final int LARGURA = 1000;
    private final int ALTURA = 750;

    private Image fundoCenario1, fundoCenario2, fundoCenario3, fundoGinasio, fundoSalaAula1, fundoBiblioteca,
            imgArmarioAberto, imgNicolas;
    private Image imgChave, imgLivro;
    private Image imgAlunoCorredor1, imgAlunoCorredor2;
    private Image imgRaquel;
    private Image[] framesAndar = new Image[2];
    private Image imgParada;
    private Image imgPortraitAudrey, imgPortraitNicollas, imgPortraitGabi, imgPortraitIvi;
    private Image imgPortraitRaquel, imgPortraitNicolas, imgPortraitCamila;

    private final int AUDREY_LARGURA = 250;
    private final int AUDREY_ALTURA = 475;
    private final int ANDAR1_LARGURA = 207;
    private final int ANDAR2_LARGURA = 217;
    private final int NPC_LARGURA = 440;
    private final int NPC_ALTURA = 500;
    private final int SALA_NPC_LARGURA = 440;
    private final int SALA_NPC_ALTURA = 500;
    private final int SALA_RAQUEL_ALTURA = 510;
    private final int SALA_NICOLAS_LARGURA = 340;
    private final int SALA_NICOLAS_ALTURA = 540;
    private final int SALA_CAMILA_ALTURA = 510;
    private int audreyX = 100, audreyY = 580, velX = 0;
    private int frameAtual = 0, contadorAnimacao = 0;

    private final int NICOLAS_LARGURA = 350;
    private final int NICOLAS_ALTURA = 520;

    private boolean olhandoDireita = true, estaMovendo = false;
    private boolean temChave = false, armarioEstaAberto = false;
    private boolean estaEmDialogoNicolas = false;
    private boolean nicolasJaFoiEncontrado = false;
    private String[] dialogoNicolasIntro = {
        "Oi! Você deve ser a aluna nova, ne? Eu sou o Nicollas, muito prazer!",
        "Que bom te conhecer, Audrey! Eu ajudo os alunos com as missões por aqui.",
        "Qualquer duvida que tiver, pode contar comigo, ta?",
        "Vamos comecar sua aventura juntos! Vai ser incrivel!"
    };
    private boolean primeiroEncontroNicolas = false;
    private boolean mostrando_chave = false;
    private boolean inventarioAberto = false;
    private boolean temLivro = false;
    private boolean livroJoiFoiPego = false;
    private boolean mostrarObjetivos = false;
    private int contadorEfeitoChave = 0;

    // --- NOVAS VARIAVEIS DE HOBBIES E DIÁRIO ---
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
    // Flags de visita por area (para exigir exploração completa)
    private boolean visitouGinasio = false;
    private boolean visitouCorredor2 = false; // mapa 3
    private boolean visitouCorredor3 = false; // mapa 4
    private boolean visitouCorredor4 = false; // mapa 5
    private boolean visitouBiblioteca = false; // mapa 6
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
    private boolean personagensNaBiblioteca = false;
    private int contadorTeleporte = -1;

    private Image imgCamila;
    private Image imgGabi;
    private Image imgGabiCorredor;
    private boolean ep1FalouGabiCorredor = false;

    // -------------------------------------------
    private int indiceMapa = 0, faseDialogoNicolas = 0, selectedDialogueOption = 0;
    private int ultimaPosAoEntraSala = 0;

    private int posArmarioX = 650;
    private int posNicolasXSalaAula = 500;
    private int posPortaX = 300;

    private int puertaSaidaX = 20;
    private int puertaSaidaLargura = 80;

    private String textoDialogo = "";
    private String nomePersonagem = "";
    private int tamanhoTextoVisivel = 0;
    private double tamanhoTextoVisivelAcumulado = 0.0;
    private int contadorTypewriter = 0;
    private String textoDialogoAnterior = "";

    private int faseDialogoLeitura = 0;
    private int faseDialogoArte = 0;
    private int faseDialogoFitness = 0;

    // --- SISTEMA DE XP ---
    private int xp = 0;
    private boolean xpRaquelDado = false;
    private boolean xpNicolasDado = false;
    private boolean xpCamilaDado = false;
    private boolean xpGabiDado = false;

    // --- DIÁLOGO AUTOMÁTICO DA SALA DE AULA ---
    // Sequencia de apresentação automática quando Audrey entra pela primeira vez
    private boolean dialogoSalaAutoIniciado = false;  // se a intro auto já começou
    private boolean dialogoSalaAutoConcluido = false; // se a intro auto já terminou
    private int faseDialogoSalaAuto = 0;              // fase atual da intro auto (0..N)
    private boolean aguardandoAvanceSalaAuto = false;  // esperando tecla F para avançar

    // --- NOTIFICAÇÃO DE MISSÃO ---
    private boolean mostrarNotificacaoMissao = false;
    private int contadorNotificacaoMissao = 0;
    private static final int NOTIFICACAO_DURACAO = 90; // Reduzido para 90 frames
    private String textoNotificacao = "";

    // --- NOTIFICAÇÃO DE XP ---
    private boolean mostrarNotificacaoXP = false;
    private int contadorNotificacaoXP = 0;
    private static final int NOTIFICACAO_XP_DURACAO = 50; // Reduzido para 50 frames
    private int xpGanho = 0;

    // --- ANIMACAO RISCADO (MISSAO CONCLUIDA) ---
    private boolean mostrarAnimacaoRiscado = false;
    private int contadorAnimacaoRiscado = 0;
    private String textoMissaoRiscada = "";

    // XP concedido só após o fim da conversa
    private int xpPendente = 0;
    private boolean temXpPendente = false;

    // --- NOVA MISSÃO: SOLIDÃO URBANA ---
    private boolean ep1_solidaoUrbanaAtiva = false;
    private boolean ep1_solidaoUrbanaConcluida = false;
    private boolean ep1_entregouDesenho = false;

    private Font fontCrayonHand;
    private BufferedImage shaderBuffer = new BufferedImage(1000, 750, BufferedImage.TYPE_INT_RGB);

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

        // XP e flags de parabéns
        props.setProperty("xp", String.valueOf(xp));
        props.setProperty("xpRaquelDado", String.valueOf(xpRaquelDado));
        props.setProperty("xpNicolasDado", String.valueOf(xpNicolasDado));
        props.setProperty("xpCamilaDado", String.valueOf(xpCamilaDado));
        // Diálogo automático da sala
        props.setProperty("dialogoSalaAutoIniciado", String.valueOf(dialogoSalaAutoIniciado));
        props.setProperty("dialogoSalaAutoConcluido", String.valueOf(dialogoSalaAutoConcluido));
        props.setProperty("faseDialogoSalaAuto", String.valueOf(faseDialogoSalaAuto));
        props.setProperty("faseDialogoLeitura", String.valueOf(faseDialogoLeitura));
        props.setProperty("faseDialogoArte", String.valueOf(faseDialogoArte));
        props.setProperty("faseDialogoFitness", String.valueOf(faseDialogoFitness));

        props.setProperty("ep1_solidaoUrbanaAtiva", String.valueOf(ep1_solidaoUrbanaAtiva));
        props.setProperty("ep1_solidaoUrbanaConcluida", String.valueOf(ep1_solidaoUrbanaConcluida));
        props.setProperty("ep1_entregouDesenho", String.valueOf(ep1_entregouDesenho));

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

        // XP e flags de parabéns
        xp = Integer.parseInt(props.getProperty("xp", "0"));
        xpRaquelDado = Boolean.parseBoolean(props.getProperty("xpRaquelDado", "false"));
        xpNicolasDado = Boolean.parseBoolean(props.getProperty("xpNicolasDado", "false"));
        xpCamilaDado = Boolean.parseBoolean(props.getProperty("xpCamilaDado", "false"));
        // Diálogo automático da sala
        dialogoSalaAutoIniciado = Boolean.parseBoolean(props.getProperty("dialogoSalaAutoIniciado", "false"));
        dialogoSalaAutoConcluido = Boolean.parseBoolean(props.getProperty("dialogoSalaAutoConcluido", "false"));
        faseDialogoSalaAuto = Integer.parseInt(props.getProperty("faseDialogoSalaAuto", "0"));
        faseDialogoLeitura = Integer.parseInt(props.getProperty("faseDialogoLeitura", "0"));
        faseDialogoArte = Integer.parseInt(props.getProperty("faseDialogoArte", "0"));
        faseDialogoFitness = Integer.parseInt(props.getProperty("faseDialogoFitness", "0"));

        ep1_solidaoUrbanaAtiva = Boolean.parseBoolean(props.getProperty("ep1_solidaoUrbanaAtiva", "false"));
        ep1_solidaoUrbanaConcluida = Boolean.parseBoolean(props.getProperty("ep1_solidaoUrbanaConcluida", "false"));
        ep1_entregouDesenho = Boolean.parseBoolean(props.getProperty("ep1_entregouDesenho", "false"));

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
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File(JogoAudrey.resolvePath("CrayonHandRegular2016Demo.ttf")))
                    .deriveFont(14f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontCrayonHand);
        } catch (Exception e) {
            fontCrayonHand = new Font("Arial", Font.PLAIN, 14);
        }
    }

    private void carregarAssets() {
        fundoCenario1 = redimensionarFundo(new ImageIcon(JogoAudrey.resolvePath("corredor1.png")).getImage(), LARGURA, ALTURA);
        fundoCenario2 = redimensionarFundo(new ImageIcon(JogoAudrey.resolvePath("corredor2.png")).getImage(), LARGURA, ALTURA);
        fundoCenario3 = redimensionarFundo(new ImageIcon(JogoAudrey.resolvePath("corredor3.png")).getImage(), LARGURA, ALTURA);
        fundoGinasio = redimensionarFundo(new ImageIcon(JogoAudrey.resolvePath("ginasio.png")).getImage(), LARGURA, ALTURA);
        fundoBiblioteca = redimensionarFundo(new ImageIcon(JogoAudrey.resolvePath("bibliotecasala4.png")).getImage(), LARGURA, ALTURA);
        fundoSalaAula1 = redimensionarFundo(new ImageIcon(JogoAudrey.resolvePath("sala de aula 1.png")).getImage(), LARGURA, ALTURA);
        imgArmarioAberto = new ImageIcon(JogoAudrey.resolvePath("imagemarmario.png")).getImage();
        imgNicolas = redimensionarImagem(new ImageIcon(JogoAudrey.resolvePath("nico__1_-removebg-preview-Photoroom.png")).getImage(),
                SALA_NICOLAS_LARGURA, SALA_NICOLAS_ALTURA);
        imgChave = new ImageIcon(JogoAudrey.resolvePath("chave.png")).getImage();
        imgLivro = new ImageIcon(JogoAudrey.resolvePath("livro.png")).getImage();

        imgAlunoCorredor1 = redimensionarImagem(new ImageIcon(JogoAudrey.resolvePath("aluno_corredor1.png")).getImage(), NPC_LARGURA,
                NPC_ALTURA);
        imgAlunoCorredor2 = redimensionarImagem(new ImageIcon(JogoAudrey.resolvePath("aluno_corredor2_backup.png")).getImage(), NPC_LARGURA,
                NPC_ALTURA);
        imgPortraitAudrey = carregarImagem("pixil-frame-0 (5)-Photoroom.png");
        imgPortraitNicollas = carregarImagem("nicollas_caixa_dialogo.png");
        imgPortraitGabi = carregarImagem("gabi caixa de dialogo-Photoroom.png");
        imgPortraitIvi = carregarImagem("1000115243-removebg-preview.png");
        imgPortraitRaquel = carregarImagem("quel_pixel_art_modified (1)-Photoroom (1).png");
        imgPortraitNicolas = carregarImagem("portrait_nicollas-removebg-preview.png");
        imgPortraitCamila = carregarImagem("camiz_pixel_art (1)-Photoroom (1).png");
        imgCamila = redimensionarImagem(new ImageIcon(
                JogoAudrey.resolvePath("a_full_body_drawing_of_the_female_character_from_data_image_image_12_showing-removebg-preview-removebg-preview-Photoroom (1).png"))
                .getImage(), SALA_NPC_LARGURA, SALA_CAMILA_ALTURA);
        imgRaquel = redimensionarImagem(
                new ImageIcon(JogoAudrey.resolvePath("pixil-frame-0__4_-removebg-preview-Photoroom.png")).getImage(),
                SALA_NPC_LARGURA, SALA_RAQUEL_ALTURA);
        imgGabi = redimensionarImagem(
                new ImageIcon(JogoAudrey.resolvePath("gabi_personagem-removebg-preview.png")).getImage(),
                SALA_NPC_LARGURA, SALA_RAQUEL_ALTURA);
        imgGabiCorredor = redimensionarImagem(
                new ImageIcon(JogoAudrey.resolvePath("pixil-frame-0-Photoroom (1).png")).getImage(),
                NPC_LARGURA, NPC_ALTURA);

        framesAndar[0] = redimensionarImagem(new ImageIcon(JogoAudrey.resolvePath("andarum.png")).getImage(), ANDAR1_LARGURA,
                AUDREY_ALTURA);
        framesAndar[1] = redimensionarImagem(new ImageIcon(JogoAudrey.resolvePath("andar02-removebg-preview.png")).getImage(), ANDAR2_LARGURA,
                AUDREY_ALTURA);

        imgParada = redimensionarImagem(
                new ImageIcon(JogoAudrey.resolvePath("parada-removebg-preview (1).png")).getImage(),
                AUDREY_LARGURA, AUDREY_ALTURA);
    }

    private Image carregarImagem(String caminho) {
        try {
            return ImageIO.read(new File(JogoAudrey.resolvePath(caminho)));
        } catch (Exception e) {
            System.err.println("[ERRO] Não foi possivel carregar: " + caminho);
            return null;
        }
    }

    private Image redimensionarImagem(Image img, int width, int height) {
        if (img == null) return null;
        java.awt.image.BufferedImage result = new java.awt.image.BufferedImage(
                width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(img, 0, 0, width, height, null);
        g2d.dispose();
        return result;
    }

    // --- CACHE DE OBJETOS DE RENDERIZAÇÃO (0 ALOCAÇÕES NO REPAINT) ---
    private static final BasicStroke STROKE_1 = new BasicStroke(1f);
    private static final BasicStroke STROKE_2 = new BasicStroke(2f);
    private static final BasicStroke STROKE_3 = new BasicStroke(3f);
    private static final BasicStroke STROKE_4 = new BasicStroke(4f);

    private static final Color COLOR_SOMBRA_CHAO = new Color(0, 0, 0, 70);
    private static final Color COLOR_LABEL_BG = new Color(253, 246, 227, 220);
    private static final Color COLOR_LABEL_BORDER = new Color(210, 180, 140);
    private static final Color COLOR_LABEL_TEXT = new Color(120, 80, 100);
    private static final Color COLOR_PASTEL_VEIL = new Color(255, 235, 240, 18);

    private final java.util.Map<String, Image> cacheRedimensionado = new java.util.HashMap<>();

    private Image getCachedResizedImage(Image img, int width, int height) {
        if (img == null) return null;
        String key = img.hashCode() + "_" + width + "_" + height;
        Image res = cacheRedimensionado.get(key);
        if (res == null) {
            res = redimensionarImagem(img, width, height);
            cacheRedimensionado.put(key, res);
        }
        return res;
    }

    private void desenharSombraChao(Graphics2D g2d, int cx, int groundY, int largura) {
        g2d.setColor(COLOR_SOMBRA_CHAO);
        g2d.fillOval(cx - largura / 2, groundY - 12, largura, 24);
    }

    private Image redimensionarFundo(Image img, int targetWidth, int targetHeight) {
        java.awt.image.BufferedImage result = new java.awt.image.BufferedImage(
                targetWidth, targetHeight, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.drawImage(img, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return result;
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

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);

        int w = getWidth();
        int h = getHeight();

        // Calcular a escala preservando a proporcao de 1000x750
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
                // Gabi no corredor 2 (X=500, longe da porta em X=350)
                if (imgGabiCorredor != null) {
                    int gabiCorredorX = 500;
                    int npcBaseY = audreyY + 150;
                    desenharSombraChao(g2d, gabiCorredorX + NPC_LARGURA / 2, npcBaseY - 55, (int) (NPC_LARGURA * 0.6));
                    g2d.drawImage(imgGabiCorredor, gabiCorredorX + NPC_LARGURA, npcBaseY - NPC_ALTURA, -NPC_LARGURA,
                            NPC_ALTURA, this);
                    int labelW = 100;
                    int labelH = 30;
                    int labelX = gabiCorredorX + (NPC_LARGURA - labelW) / 2;
                    int labelY = npcBaseY - NPC_ALTURA - labelH - 5;
                    g2d.setColor(new Color(253, 246, 227, 220));
                    g2d.fillRoundRect(labelX, labelY, labelW, labelH, 12, 12);
                    g2d.setColor(new Color(210, 180, 140));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(labelX, labelY, labelW, labelH, 12, 12);
                    g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 14f));
                    g2d.setColor(new Color(120, 80, 100));
                    FontMetrics fmG = g2d.getFontMetrics();
                    g2d.drawString("Gabi", labelX + (labelW - fmG.stringWidth("Gabi")) / 2, labelY + labelH - 8);
                }
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

            // Desenhar NPCs da sala de aula
            if (indiceMapa == 2) {
                // Offset para descer os NPCs ao n\u00edvel do ch\u00e3o (mesmo que Audrey)
                int npcBaseY = audreyY + 150;
                if (imgRaquel != null) {
                    desenharSombraChao(g2d, 200 + SALA_NPC_LARGURA / 2, npcBaseY - 55, (int) (SALA_NPC_LARGURA * 0.6));
                    g2d.drawImage(imgRaquel, 200 + SALA_NPC_LARGURA, npcBaseY - SALA_RAQUEL_ALTURA, -SALA_NPC_LARGURA,
                            SALA_RAQUEL_ALTURA, this);
                    int labelW = 110;
                    int labelH = 30;
                    int labelX = 200 + (SALA_NPC_LARGURA - labelW) / 2;
                    int labelY = npcBaseY - SALA_RAQUEL_ALTURA - labelH - 5;
                    g2d.setColor(new Color(253, 246, 227, 220));
                    g2d.fillRoundRect(labelX, labelY, labelW, labelH, 12, 12);
                    g2d.setColor(new Color(210, 180, 140));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(labelX, labelY, labelW, labelH, 12, 12);
                    g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 14f));
                    g2d.setColor(new Color(120, 80, 100));
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString("Raquel", labelX + (labelW - fm.stringWidth("Raquel")) / 2, labelY + labelH - 8);
                }
                if (imgNicolas != null) {
                    desenharSombraChao(g2d, 340 + SALA_NICOLAS_LARGURA / 2, npcBaseY - 55, (int) (SALA_NICOLAS_LARGURA * 0.6));
                    g2d.drawImage(imgNicolas, 340 + SALA_NICOLAS_LARGURA, npcBaseY - SALA_NICOLAS_ALTURA,
                            -SALA_NICOLAS_LARGURA, SALA_NICOLAS_ALTURA, this);
                    int labelW = 120;
                    int labelH = 30;
                    int labelX = 340 + (SALA_NICOLAS_LARGURA - labelW) / 2;
                    int labelY = npcBaseY - SALA_NICOLAS_ALTURA - labelH - 5;
                    g2d.setColor(new Color(253, 246, 227, 220));
                    g2d.fillRoundRect(labelX, labelY, labelW, labelH, 12, 12);
                    g2d.setColor(new Color(210, 180, 140));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(labelX, labelY, labelW, labelH, 12, 12);
                    g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 14f));
                    g2d.setColor(new Color(120, 80, 100));
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString("Nicolas", labelX + (labelW - fm.stringWidth("Nicolas")) / 2, labelY + labelH - 8);
                }
                if (imgCamila != null) {
                    desenharSombraChao(g2d, 480 + SALA_NPC_LARGURA / 2, npcBaseY - 55, (int) (SALA_NPC_LARGURA * 0.6));
                    g2d.drawImage(imgCamila, 480 + SALA_NPC_LARGURA, npcBaseY - SALA_CAMILA_ALTURA, -SALA_NPC_LARGURA,
                            SALA_CAMILA_ALTURA, this);
                    int labelW = 110;
                    int labelH = 30;
                    int labelX = 480 + (SALA_NPC_LARGURA - labelW) / 2;
                    int labelY = npcBaseY - SALA_CAMILA_ALTURA - labelH - 5;
                    g2d.setColor(new Color(253, 246, 227, 220));
                    g2d.fillRoundRect(labelX, labelY, labelW, labelH, 12, 12);
                    g2d.setColor(new Color(210, 180, 140));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(labelX, labelY, labelW, labelH, 12, 12);
                    g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 14f));
                    g2d.setColor(new Color(120, 80, 100));
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString("Camila", labelX + (labelW - fm.stringWidth("Camila")) / 2, labelY + labelH - 8);
                }
            }


            // Desenhar NPCs da biblioteca
            if (indiceMapa == 6 && personagensNaBiblioteca) {
                int npcBaseY = audreyY + 150;
                // Gabi na posição X=550
                if (imgGabi != null) {
                    int gX = 500;
                    desenharSombraChao(g2d, gX + SALA_NPC_LARGURA / 2, npcBaseY - 55, (int) (SALA_NPC_LARGURA * 0.6));
                    g2d.drawImage(imgGabi, gX + SALA_NPC_LARGURA, npcBaseY - SALA_RAQUEL_ALTURA, -SALA_NPC_LARGURA,
                            SALA_RAQUEL_ALTURA, this);
                    int labelW = 100;
                    int labelH = 30;
                    int labelX = gX + (SALA_NPC_LARGURA - labelW) / 2;
                    int labelY = npcBaseY - SALA_RAQUEL_ALTURA - labelH - 5;
                    g2d.setColor(new Color(253, 246, 227, 220));
                    g2d.fillRoundRect(labelX, labelY, labelW, labelH, 12, 12);
                    g2d.setColor(new Color(210, 180, 140));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(labelX, labelY, labelW, labelH, 12, 12);
                    g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 14f));
                    g2d.setColor(new Color(120, 80, 100));
                    FontMetrics fmG = g2d.getFontMetrics();
                    g2d.drawString("Gabi", labelX + (labelW - fmG.stringWidth("Gabi")) / 2, labelY + labelH - 8);
                }
                // Ivi na posição X=300
                if (imgPortraitIvi != null) {
                    int iX = 250;
                    desenharSombraChao(g2d, iX + SALA_NPC_LARGURA / 2, npcBaseY - 55, (int) (SALA_NPC_LARGURA * 0.6));
                    g2d.drawImage(imgPortraitIvi, iX + SALA_NPC_LARGURA, npcBaseY - SALA_RAQUEL_ALTURA, -SALA_NPC_LARGURA,
                            SALA_RAQUEL_ALTURA, this);
                    int labelW = 80;
                    int labelH = 30;
                    int labelX = iX + (SALA_NPC_LARGURA - labelW) / 2;
                    int labelY = npcBaseY - SALA_RAQUEL_ALTURA - labelH - 5;
                    g2d.setColor(new Color(253, 246, 227, 220));
                    g2d.fillRoundRect(labelX, labelY, labelW, labelH, 12, 12);
                    g2d.setColor(new Color(210, 180, 140));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(labelX, labelY, labelW, labelH, 12, 12);
                    g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 14f));
                    g2d.setColor(new Color(120, 80, 100));
                    FontMetrics fmI = g2d.getFontMetrics();
                    g2d.drawString("Ivi", labelX + (labelW - fmI.stringWidth("Ivi")) / 2, labelY + labelH - 8);
                }
            }


            Image img;
            int imgLargura;
            int drawY = audreyY - AUDREY_ALTURA;

            desenharSombraChao(g2d, audreyX + AUDREY_LARGURA / 2, audreyY + 95, (int) (AUDREY_LARGURA * 0.55));

            if (!estaMovendo && imgParada != null) {
                img = imgParada;
                imgLargura = AUDREY_LARGURA;
                drawY += 150;
            } else {
                img = framesAndar[frameAtual];
                imgLargura = AUDREY_LARGURA;
                drawY += 150;
            }

            if (olhandoDireita) {
                g2d.drawImage(img, audreyX, drawY, imgLargura, AUDREY_ALTURA, this);
            } else {
                g2d.drawImage(img, audreyX + imgLargura, drawY, -imgLargura, AUDREY_ALTURA,
                        this);
            }

            // --- SHADER: Tom Pastel Levissimo ---
            // Veu rosa-creme uniforme, quase invisivel, só aquece as cores
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

        // Notifica\u00e7\u00e3o: Missao Adicionada ao Diario
        if (mostrarNotificacaoMissao) {
            desenharNotificacao(g2d, textoNotificacao, new Color(80, 160, 80), new Color(200, 255, 200), true);
        }
        // Notifica\u00e7\u00e3o: XP ganho
        if (mostrarNotificacaoXP) {
            desenharNotificacao(g2d, "+" + xpGanho + " XP ganho!", new Color(180, 100, 10), new Color(255, 220, 100), false);
        }
        
        if (mostrarAnimacaoRiscado) {
            desenharAnimacaoRiscado(g2d);
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

            // Agora desenha na tela real com as devidas proporcoes/bordas
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
            Image livroRedimensionado = getCachedResizedImage(imgLivro, 80, 100);
            int livroX = LARGURA / 2 - 40;
            int livroY = ALTURA / 2 - 50;
            g2d.drawImage(livroRedimensionado, livroX, livroY, this);

            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 18));
            g2d.setColor(new Color(255, 215, 0));
            g2d.drawString("Livro encontrado!", LARGURA / 2 - 90, ALTURA / 2 + 80);

            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.PLAIN, 14));
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

        g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 32));
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString("INVENTÁRIO", caixaX + 150, caixaY + 50);

        g2d.setColor(new Color(200, 200, 0));
        g2d.drawLine(caixaX + 20, caixaY + 70, caixaX + caixaLargura - 20, caixaY + 70);

        int itemY = caixaY + 120;
        int itemCount = 0;

        if (temChave) {
            if (imgChave != null) {
                Image chaveGrande = getCachedResizedImage(imgChave, 60, 60);
                g2d.drawImage(chaveGrande, caixaX + 50, itemY - 30, this);
            }

            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 20));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Chave do Armário", caixaX + 130, itemY + 10);

            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.PLAIN, 14));
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawString("Obtida de: Garoto com livro (sala de aula)", caixaX + 130, itemY + 35);

            itemY += 80;
            itemCount++;
        }

        if (temLivro) {
            if (imgLivro != null) {
                Image livroGrande = getCachedResizedImage(imgLivro, 60, 60);
                g2d.drawImage(livroGrande, caixaX + 50, itemY - 30, this);
            }

            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 20));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Livro Misterioso", caixaX + 130, itemY + 10);

            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.PLAIN, 14));
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawString("Encontrado no armário", caixaX + 130, itemY + 35);

            itemCount++;
            itemY += 80;
        }

        if (temCadernoEsboco) {
            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 20));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Caderno de Esbocos", caixaX + 130, itemY + 10);
            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.PLAIN, 14));
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawString("Obtido de: Lider Arte", caixaX + 130, itemY + 35);
            itemCount++;
            itemY += 80;
        }

        if (temCronograma) {
            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 20));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Cronograma de Treinos", caixaX + 130, itemY + 10);
            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.PLAIN, 14));
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawString("Obtido de: Lider Fitness", caixaX + 130, itemY + 35);
            itemCount++;
            itemY += 80;
        }

        if (itemCount == 0) {
            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.PLAIN, 18));
            g2d.setColor(new Color(200, 100, 100));
            g2d.drawString("Inventário vazio", caixaX + 250, itemY + 50);
        }

        g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.PLAIN, 16));
        g2d.setColor(new Color(150, 150, 150));
        g2d.drawString("Pressione [B] para fechar o inventario", caixaX + 50, caixaY + caixaAltura - 20);
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

        g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 14));
        g2d.setColor(new Color(255, 150, 150));
        g2d.drawString("OBJETIVOS", objetivosX + 30, objetivosY + 25);

        g2d.setColor(new Color(200, 100, 100));
        g2d.drawLine(objetivosX + 10, objetivosY + 35, objetivosX + objetivosLargura - 10, objetivosY + 35);

        int yOffset = objetivosY + 60;
        g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.PLAIN, 11));

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
                Image chaveEfeito = getCachedResizedImage(imgChave, tamanhoCh, tamanhoCh);
                g2d.drawImage(chaveEfeito, x - tamanhoCh / 2, y - tamanhoCh / 2, this);
            }

            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 24));
            g2d.setColor(new Color(255, 215, 0));
            g2d.drawString("COLETADO!", x - 70, y + 100);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    private void desenharInterface(Graphics2D g2d) {
        if (!armarioEstaAberto && textoDialogo.isEmpty()) {
            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 18));

            String teclaE = Configuracoes.getInstance().getNomeTecla("INTERAGIR");
            String teclaF = Configuracoes.getInstance().getNomeTecla("FALAR");

            if (indiceMapa == 0 && Math.abs(audreyX - posArmarioX) < 150) {
                desenharTextoComSombra(g2d, "Pressione [" + teclaE + "] para abrir", audreyX - 10, audreyY - 430,
                        new Color(160, 120, 200));
            }

            if (indiceMapa == 1 && !personagensNaBiblioteca) {
                // Hint da porta (só aparece se NÃO estiver perto da Gabi)
                if (Math.abs(audreyX - posPortaX) < 150 && Math.abs(audreyX - 500) >= 200) {
                    if (explorouCorredor) {
                        desenharTextoComSombra(g2d, "Pressione [" + teclaE + "] para entrar", audreyX - 10,
                                audreyY - 430, new Color(160, 120, 200));
                    } else {
                        desenharTextoComSombra(g2d, "Vou explorar o final do corredor primeiro...", audreyX - 10,
                                audreyY - 430, new Color(140, 100, 180));
                    }
                }
                // Hint da Gabi (só aparece se NÃO estiver perto da porta)
                if (Math.abs(audreyX - 500) < 150 && Math.abs(audreyX - posPortaX) >= 200) {
                    desenharTextoComSombra(g2d, "Pressione [" + teclaE + "] para falar com Gabi",
                            audreyX - 10, audreyY - 430, new Color(160, 120, 200));
                }
            }

            if (indiceMapa == 2 && Math.abs(audreyX - posNicolasXSalaAula) < 120) {
                desenharTextoComSombra(g2d, "Pressione [" + teclaF + "] para falar", audreyX - 10, audreyY - 430,
                        new Color(160, 120, 200));
            }

            if (indiceMapa == 2 && estaProximoDaPuerta()) {
                desenharTextoComSombra(g2d, "Pressione [" + teclaE + "] para sair", audreyX - 10, audreyY - 430,
                        new Color(160, 120, 200));
            }

            if (indiceMapa == 3) {
                if (Math.abs(audreyX - 350) < 150) {
                    desenharTextoComSombra(g2d, "Pressione [" + teclaE + "] para abrir", audreyX - 10, audreyY - 430,
                            new Color(160, 120, 200));
                } else if (Math.abs(audreyX - 750) < 150) {
                    desenharTextoComSombra(g2d, "Pressione [" + teclaE + "] para entrar na biblioteca", audreyX - 10,
                            audreyY - 430, new Color(160, 120, 200));
                }
            }

            if (indiceMapa == 6 && Math.abs(audreyX - 50) < 150) {
                desenharTextoComSombra(g2d, "Pressione [" + teclaE + "] para sair da biblioteca", audreyX - 10,
                        audreyY - 430, new Color(160, 120, 200));
            }

            if (indiceMapa == 6 && personagensNaBiblioteca) {
                if (Math.abs(audreyX - 550) < 150) {
                    desenharTextoComSombra(g2d, "Pressione [" + teclaE + "] para falar com Gabi",
                            audreyX - 10, audreyY - 430, new Color(160, 120, 200));
                } else if (Math.abs(audreyX - 300) < 150) {
                    desenharTextoComSombra(g2d, "Pressione [" + teclaE + "] para falar com Ivi",
                            audreyX - 10, audreyY - 430, new Color(160, 120, 200));
                }
            }
        }

        if (!textoDialogo.isEmpty() || armarioEstaAberto) {
            int caixaW = 860;
            int caixaH = 130;
            int caixaX = (LARGURA - caixaW) / 2;
            int caixaY = ALTURA - caixaH - 30;

            // --- PORTRAIT EM ESTILO POLAROID / QUADRO DE PAPEL ---
            Image portraitImg = null;
            if ("Audrey".equals(nomePersonagem)) {
                portraitImg = imgPortraitAudrey; 
            }else if ("Nicolas".equals(nomePersonagem)) {
                portraitImg = (imgPortraitNicollas != null) ? imgPortraitNicollas : imgPortraitNicolas; 
            }else if ("Raquel".equals(nomePersonagem)) {
                portraitImg = imgPortraitRaquel; 
            }else if ("Camila".equals(nomePersonagem)) {
                portraitImg = imgPortraitCamila; 
            }else if ("Gabi".equals(nomePersonagem)) {
                portraitImg = imgPortraitGabi; 
            }else if ("Ivi".equals(nomePersonagem)) {
                portraitImg = imgPortraitIvi;
            }

            int textXOffset = caixaX + 30;

            if (portraitImg != null) {
                int polaroidW = 160;
                int polaroidH = 190;
                int polaroidX = caixaX + 15;
                int polaroidY = caixaY - 130;

                // Sombra projetada da Polaroid
                g2d.setColor(new Color(0, 0, 0, 110));
                g2d.fillRoundRect(polaroidX + 5, polaroidY + 5, polaroidW, polaroidH, 12, 12);

                // Moldura de Papel Polaroid (Tom lilás envelhecido)
                g2d.setColor(new Color(175, 155, 175));
                g2d.fillRoundRect(polaroidX, polaroidY, polaroidW, polaroidH, 12, 12);
                g2d.setColor(new Color(40, 30, 45));
                g2d.setStroke(new BasicStroke(2.5f));
                g2d.drawRoundRect(polaroidX, polaroidY, polaroidW, polaroidH, 12, 12);

                // Área interna da foto
                int fotoX = polaroidX + 10;
                int fotoY = polaroidY + 10;
                int fotoW = polaroidW - 20;
                int fotoH = polaroidH - 35;

                g2d.setColor(new Color(30, 22, 35));
                g2d.fillRect(fotoX, fotoY, fotoW, fotoH);

                // Desenhar a imagem do portrait no polaroid (preservando proporção)
                Shape oldClip = g2d.getClip();
                g2d.setClip(new Rectangle(fotoX, fotoY, fotoW, fotoH));

                int imgW = portraitImg.getWidth(this);
                int imgH = portraitImg.getHeight(this);
                if (imgW > 0 && imgH > 0) {
                    double scaleW = (double) fotoW / imgW;
                    double scaleH = (double) fotoH / imgH;
                    double scale = Math.max(scaleW, scaleH); // preenche sem esmagar
                    int drawW = (int) (imgW * scale);
                    int drawH = (int) (imgH * scale);
                    int drawX = fotoX + (fotoW - drawW) / 2;
                    int drawY = fotoY + (fotoH - drawH) / 2;
                    g2d.drawImage(portraitImg, drawX, drawY, drawW, drawH, this);
                } else {
                    g2d.drawImage(portraitImg, fotoX, fotoY, fotoW, fotoH, this);
                }
                g2d.setClip(oldClip);

                // Borda interna da foto
                g2d.setColor(new Color(60, 48, 65));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRect(fotoX, fotoY, fotoW, fotoH);

                textXOffset = polaroidX + polaroidW + 25;
            }

            // --- CAIXA DE DIÁLOGO PRINCIPAL (FUNDO ESCURO E BORDA RÚSTICA) ---
            // Sombra externa
            g2d.setColor(new Color(0, 0, 0, 130));
            g2d.fillRoundRect(caixaX + 4, caixaY + 4, caixaW, caixaH, 16, 16);

            // Fundo Roxo Escuro / Indie Gothic
            g2d.setColor(new Color(24, 18, 28, 240));
            g2d.fillRoundRect(caixaX, caixaY, caixaW, caixaH, 16, 16);

            // Borda Dupla Estilo Giz/Desenho
            g2d.setColor(new Color(180, 165, 195, 220));
            g2d.setStroke(new BasicStroke(2.5f));
            g2d.drawRoundRect(caixaX, caixaY, caixaW, caixaH, 16, 16);

            g2d.setColor(new Color(100, 85, 115, 140));
            g2d.setStroke(new BasicStroke(1.2f));
            g2d.drawRoundRect(caixaX + 4, caixaY + 4, caixaW - 8, caixaH - 8, 12, 12);

            // Doodles / Rabiscos nos Cantos da Caixa de Diálogo (Estrelas / Lua / Olho)
            g2d.setColor(new Color(170, 150, 185, 150));
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2d.drawString("✦", caixaX + 15, caixaY + 25);
            g2d.drawString("★", caixaX + 25, caixaY + caixaH - 15);
            g2d.drawString("✧", caixaX + caixaW - 35, caixaY + 25);
            g2d.drawString("👁", caixaX + caixaW - 35, caixaY + caixaH - 15);
            g2d.drawString("☽", caixaX + caixaW - 55, caixaY + caixaH - 15);

            // --- TAG DE NOME DO PERSONAGEM (BADGE ESTILO PLACA) ---
            if (!nomePersonagem.isEmpty() && !armarioEstaAberto) {
                String nomeUpper = nomePersonagem.toUpperCase();
                g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 15f));
                FontMetrics fmNome = g2d.getFontMetrics();
                int tagW = Math.max(100, fmNome.stringWidth(nomeUpper) + 30);
                int tagH = 26;
                int tagX = textXOffset;
                int tagY = caixaY - 14;

                // Fundo da Tag
                g2d.setColor(new Color(15, 12, 20, 250));
                g2d.fillRoundRect(tagX, tagY, tagW, tagH, 8, 8);

                // Borda da Tag
                g2d.setColor(new Color(210, 195, 225));
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawRoundRect(tagX, tagY, tagW, tagH, 8, 8);

                // Texto do Nome
                g2d.setColor(new Color(245, 240, 250));
                g2d.drawString(nomeUpper, tagX + (tagW - fmNome.stringWidth(nomeUpper)) / 2, tagY + 18);
            }

            // --- TEXTO DO DIÁLOGO ---
            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 18f));
            String msg = armarioEstaAberto ? "Pressione 'Q' para fechar o armário." : textoDialogo;
            if (!armarioEstaAberto && !textoDialogo.isEmpty()) {
                msg = msg.substring(0, Math.min(msg.length(), tamanhoTextoVisivel));
                if (tamanhoTextoVisivel < textoDialogo.length() || (System.currentTimeMillis() / 400) % 2 == 0) {
                    msg += " █";
                }
            }

            if (faseDialogoNicolas == 100 && tamanhoTextoVisivel >= textoDialogo.length()) {
                msg = ""; // Oculta o diálogo para deixar apenas as alternativas limpas
            }

            g2d.setColor(new Color(240, 235, 245));
            int maxWidth = caixaX + caixaW - textXOffset - 40;
            desenharTextoQuebrado(g2d, msg, textXOffset, caixaY + 45, maxWidth);

            // --- ÍCONE / SETA DE AVANÇAR (▼) OU OPÇÕES DE DIÁLOGO NO CENTRO INFERIOR ---
            if (!armarioEstaAberto && !textoDialogo.isEmpty()) {
                if (faseDialogoNicolas == 100 && tamanhoTextoVisivel >= textoDialogo.length()) {
                    g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 17f));
                    
                    String opt1 = "Já terminei meu desenho! (Enviar imagem)";
                    String opt2 = "Ainda vou fazer o desenho.";
                    
                    int optX = textXOffset + 20;
                    int optY1 = caixaY + 55;
                    int optY2 = caixaY + 85;
                    
                    // Opção 1
                    if (selectedDialogueOption == 0) {
                        g2d.setColor(new Color(180, 255, 210));
                        g2d.drawString("▶ " + opt1, optX - 15, optY1);
                    } else {
                        g2d.setColor(new Color(160, 150, 180, 160));
                        g2d.drawString("  " + opt1, optX - 15, optY1);
                    }
                    
                    // Opção 2
                    if (selectedDialogueOption == 1) {
                        g2d.setColor(new Color(255, 220, 130));
                        g2d.drawString("▶ " + opt2, optX - 15, optY2);
                    } else {
                        g2d.setColor(new Color(160, 150, 180, 160));
                        g2d.drawString("  " + opt2, optX - 15, optY2);
                    }
                } else {
                    int arrowX = caixaX + caixaW / 2;
                    int arrowY = caixaY + caixaH - 8;
                    int[] px = {arrowX - 8, arrowX + 8, arrowX};
                    int[] py = {arrowY - 8, arrowY - 8, arrowY};
                    g2d.setColor(new Color(220, 205, 235));
                    g2d.drawPolygon(px, py, 3);
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
        g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 18));
        g2d.setColor(Color.BLACK);
        g2d.drawString(t, x + 2, y + 2);
        g2d.setColor(c);
        g2d.drawString(t, x, y);
    }

    private void desenharNotificacao(Graphics2D g2d, String texto, Color corFundo, Color corTexto, boolean noTopo) {
        g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 20f));
        FontMetrics fmN = g2d.getFontMetrics();
        int notifW = Math.max(400, fmN.stringWidth(texto) + 90);
        int notifH = 52;

        // Calcular alpha e progresso de animação
        float alpha;
        int contador = noTopo ? contadorNotificacaoMissao : contadorNotificacaoXP;
        int duracao = noTopo ? NOTIFICACAO_DURACAO : NOTIFICACAO_XP_DURACAO;
        int fadeIn = 8; // Reduzido de 12 para 8
        int fadeOut = 10; // Reduzido de 15 para 10

        if (contador < fadeIn) {
            alpha = contador / (float) fadeIn;
        } else if (contador > duracao - fadeOut) {
            alpha = (duracao - contador) / (float) fadeOut;
        } else {
            alpha = 1f;
        }
        alpha = Math.max(0f, Math.min(1f, alpha));

        // Curva de Easing (Quadratic Ease-Out) para suavizar a transição do slide
        float ease = alpha * (2f - alpha);

        // Calcular offset de slide
        int offsetX = 0, offsetY = 0;
        if (!noTopo) {
            // XP: desliza da direita para dentro e depois para fora pela direita
            if (contador < fadeIn) {
                offsetX = (int) ((1.0f - ease) * (notifW + 40));
            } else if (contador > duracao - fadeOut) {
                offsetX = (int) ((1.0f - ease) * (notifW + 40));
            }
        } else {
            // Missão: desliza de cima para baixo e depois volta para cima
            if (contador < fadeIn) {
                offsetY = (int) ((1.0f - ease) * -(notifH + 30));
            } else if (contador > duracao - fadeOut) {
                offsetY = (int) ((1.0f - ease) * -(notifH + 30));
            }
        }

        int notifX, notifY;
        if (noTopo) {
            notifX = (LARGURA - notifW) / 2;
            notifY = 20 + offsetY;
        } else {
            notifX = LARGURA - notifW - 30 + offsetX;
            notifY = ALTURA - notifH - 30;
        }

        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        Composite oldComp = g2d.getComposite();
        g2d.setComposite(ac);

        // Sombra
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRoundRect(notifX + 4, notifY + 4, notifW, notifH, 18, 18);
        // Fundo
        g2d.setColor(corFundo);
        g2d.fillRoundRect(notifX, notifY, notifW, notifH, 18, 18);
        // Borda
        g2d.setColor(corTexto.darker());
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(notifX, notifY, notifW, notifH, 18, 18);
        g2d.setStroke(new BasicStroke(1f));
        // Ícone
        g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 20f));
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.drawString("✓", notifX + 14, notifY + notifH / 2 + 7);
        // Texto com sombra leve
        g2d.setColor(new Color(0, 0, 0, 90));
        g2d.drawString(texto, notifX + 42 + 1, notifY + notifH / 2 + 7 + 1);
        g2d.setColor(corTexto);
        g2d.drawString(texto, notifX + 42, notifY + notifH / 2 + 7);

        g2d.setComposite(oldComp);
    }

    private void desenharAnimacaoRiscado(Graphics2D g2d) {
        g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 22f));
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(textoMissaoRiscada) + 40;
        int height = 50;
        
        // Canto superior direito
        int x = LARGURA - width - 20;
        int y = 80;

        // Fundo estilo papel
        g2d.setColor(new Color(255, 250, 240, 220));
        g2d.fillRoundRect(x, y, width, height, 10, 10);
        g2d.setColor(new Color(200, 180, 150, 220));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(x, y, width, height, 10, 10);

        // Texto
        g2d.setColor(new Color(100, 50, 50));
        g2d.drawString(textoMissaoRiscada, x + 20, y + 32);

        // Animação do risco
        if (contadorAnimacaoRiscado > 10) {
            int progressoRisco = Math.min(20, contadorAnimacaoRiscado - 10);
            float proporcao = (float) progressoRisco / 20f;
            int endX = x + 10 + (int)((width - 20) * proporcao);
            
            g2d.setColor(new Color(200, 30, 30, 200));
            g2d.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            // Desenhar um leve zigue-zague ou linha inclinada
            int startY = y + height / 2 + 5;
            int endY = y + height / 2 - 5 + (int)(Math.sin(progressoRisco * 0.3) * 3);
            g2d.drawLine(x + 10, startY, endX, endY);
        }
    }

    private boolean estaProximoDaPuerta() {
        return indiceMapa == 2
                && audreyX < puertaSaidaX + puertaSaidaLargura + 50
                && audreyX + AUDREY_LARGURA > puertaSaidaX - 50;
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
        // --- ANIMAÇÃO TYPEWRITER E SINCRONIZAÇÃO DE ÁUDIO ---
        if (!textoDialogo.isEmpty()) {
            if (!textoDialogo.equals(textoDialogoAnterior)) {
                textoDialogoAnterior = textoDialogo;
                tamanhoTextoVisivel = 0;
                tamanhoTextoVisivelAcumulado = 0.0;
                contadorTypewriter = 0;
            }

            if (tamanhoTextoVisivel < textoDialogo.length()) {
                tamanhoTextoVisivelAcumulado += 0.8; // avança 0.8 letras a cada frame (mais rápido)
                int novoTamanho = (int) tamanhoTextoVisivelAcumulado;
                if (novoTamanho > tamanhoTextoVisivel) {
                    tamanhoTextoVisivel = Math.min(textoDialogo.length(), novoTamanho);
                    char charAtual = textoDialogo.charAt(tamanhoTextoVisivel - 1);

                    // Espaços e pontuação = silêncio entre palavras (sincroniza áudio com a fala)
                    if (charAtual == ' ' || charAtual == '.' || charAtual == ',' || charAtual == '!' 
                        || charAtual == '?' || charAtual == ':' || charAtual == ';' || charAtual == '—' || charAtual == '-') {
                        GerenciadorAudio.pararVozNicolas();
                        GerenciadorAudio.pararVozRaquel();
                    } else {
                        if ("Nicolas".equals(nomePersonagem)) {
                            GerenciadorAudio.tocarVozNicolas();
                            GerenciadorAudio.pararVozRaquel();
                        } else if ("Raquel".equals(nomePersonagem)) {
                            GerenciadorAudio.tocarVozRaquel();
                            GerenciadorAudio.pararVozNicolas();
                        } else {
                            GerenciadorAudio.pararVozNicolas();
                            GerenciadorAudio.pararVozRaquel();
                        }
                    }
                }
            } else {
                // A frase terminou de aparecer por completo: interrompe o áudio da fala imediatamente
                GerenciadorAudio.pararVozNicolas();
                GerenciadorAudio.pararVozRaquel();
            }
        } else {
            if (!textoDialogoAnterior.isEmpty()) {
                textoDialogoAnterior = "";
                tamanhoTextoVisivel = 0;
                GerenciadorAudio.pararVozNicolas();
                GerenciadorAudio.pararVozRaquel();
            }
        }

        if (mostrando_chave) {
            contadorEfeitoChave++;
            if (contadorEfeitoChave >= 60) {
                mostrando_chave = false;
                contadorEfeitoChave = 0;
            }
        }

        // Contadores de notifica\u00e7\u00e3o
        if (mostrarNotificacaoMissao) {
            contadorNotificacaoMissao++;
            if (contadorNotificacaoMissao >= NOTIFICACAO_DURACAO) {
                mostrarNotificacaoMissao = false;
                contadorNotificacaoMissao = 0;
            }
        }
        if (mostrarNotificacaoXP) {
            contadorNotificacaoXP++;
            if (contadorNotificacaoXP >= NOTIFICACAO_XP_DURACAO) {
                mostrarNotificacaoXP = false;
                contadorNotificacaoXP = 0;
            }
        }

        if (mostrarAnimacaoRiscado) {
            contadorAnimacaoRiscado++;
            if (contadorAnimacaoRiscado >= 50) { // menos de 1 segundo
                mostrarAnimacaoRiscado = false;
                contadorAnimacaoRiscado = 0;
            }
        }

        if (contadorTeleporte >= 0 && indiceMapa == 2) {
            contadorTeleporte++;
            if (contadorTeleporte >= 600 && !personagensNaBiblioteca) {
                personagensNaBiblioteca = true;
                contadorTeleporte = -1;
            }
        }

        if (!armarioEstaAberto) {
            audreyX += velX;

            indiceMapa_public = indiceMapa;
            audreyX_public = audreyX;

            if (indiceMapa == 0) {
                visitouGinasio = true;
                if (audreyX < 0) {
                    audreyX = 0;
                }
                if (audreyX > LARGURA) {
                    indiceMapa = 1;
                    audreyX = 0;
                    textoDialogo = "";
                    nomePersonagem = "";
                    estaEmDialogoNicolas = false;
                    GerenciadorAudio.pararVozNicolas();
                    faseDialogoNicolas = 0;
                    if (!sala1Aberta) {
                        sala1Aberta = true;
                        GerenciadorAudio.tocarSomSinalEscolar();
                    }
                }
            } else if (indiceMapa == 1) {
                // Corredor 1: não conta como área obrigatória separada
                if (audreyX < 0) {
                    audreyX = 0;
                    if (velX < 0) {
                        indiceMapa = 0;
                        audreyX = LARGURA - AUDREY_LARGURA;
                        textoDialogo = "";
                        nomePersonagem = "";
                        estaEmDialogoNicolas = false;
                        GerenciadorAudio.pararVozNicolas();
                        faseDialogoNicolas = 0;
                    }
                }
                if (audreyX + AUDREY_LARGURA > LARGURA) {
                    indiceMapa = 3;
                    audreyX = 0;
                    textoDialogo = "";
                    nomePersonagem = "";
                    estaEmDialogoNicolas = false;
                    GerenciadorAudio.pararVozNicolas();
                }
            } else if (indiceMapa == 3) {
                visitouCorredor2 = true;
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
                visitouCorredor3 = true;
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
                visitouCorredor4 = true;
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
                //if (!cutsceneSalaVista) {
                //    cutsceneSalaVista = true;
                //    contadorTeleporte = 0;
                //    frame.irParaCutscene(1);
                //}

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
                // Disparar di\u00e1logo autom\u00e1tico ao entrar na sala de aula pela primeira vez
                if (!dialogoSalaAutoIniciado) {
                    dialogoSalaAutoIniciado = true;
                    faseDialogoSalaAuto = 1; // Já inicia no passo 1 do switch da tecla F
                    aguardandoAvanceSalaAuto = true;
                    estaEmDialogoNicolas = true;
                    nomePersonagem = "Audrey";
                    GerenciadorAudio.tocarSomDialogo();
                    textoDialogo = "\u2014 Oi, com licen\u00e7a... Sou a Audrey, aluna nova.";

                    // Primeira conversa com o grupo (Raquel + Nicolas + Camila)
                    if (!xpRaquelDado && !xpNicolasDado && !xpCamilaDado) {
                        xpRaquelDado = true;
                        xpNicolasDado = true;
                        xpCamilaDado = true;
                        adicionarXpPendente(3);
                    }
                }

                // Checar distancia dos 3 grupos e esconder dialogo se longe (só após intro)
                if (dialogoSalaAutoConcluido && !estaEmDialogoNicolas) {
                    if (Math.abs(audreyX - 250) > 220 && Math.abs(audreyX - 390) > 220 && Math.abs(audreyX - 530) > 220) {
                        textoDialogo = "";
                        nomePersonagem = "";
                    }
                }
            }

            if (indiceMapa == 0 && Math.abs(audreyX - posArmarioX) > 200) {
                textoDialogo = "";
                nomePersonagem = "";
            }

            if (indiceMapa == 6 && Math.abs(audreyX - 300) > 200 && Math.abs(audreyX - 550) > 200) {
                if (!estaEmDialogoNicolas) {
                    textoDialogo = "";
                    nomePersonagem = "";
                }
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

        if (code == Configuracoes.getInstance().getTecla("INVENTÁRIO")) {
            inventarioAberto = !inventarioAberto;
            return;
        }

        // Navegação de opções de diálogo quando faseDialogoNicolas for 100
        if (faseDialogoNicolas == 100 && tamanhoTextoVisivel >= textoDialogo.length()) {
            if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W || code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                selectedDialogueOption = 0;
                repaint();
                return;
            }
            if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S || code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                selectedDialogueOption = 1;
                repaint();
                return;
            }
        }

        // M para mostrar/esconder objetivos
        if (code == Configuracoes.getInstance().getTecla("OBJETIVOS")) {
            mostrarObjetivos = !mostrarObjetivos;
            return;
        }

        if (code == Configuracoes.getInstance().getTecla("DIREITA")) {
            boolean emDialogoForcado = aguardandoAvanceSalaAuto || faseDialogoNicolas >= 100;
            if (!emDialogoForcado) {
                velX = (indiceMapa == 2) ? 20 : 12;
                olhandoDireita = true;
                estaMovendo = true;
                GerenciadorAudio.tocarSomPassos();
            }
        }

        if (code == Configuracoes.getInstance().getTecla("ESQUERDA")) {
            boolean emDialogoForcado = aguardandoAvanceSalaAuto || faseDialogoNicolas >= 100;
            if (!emDialogoForcado) {
                velX = (indiceMapa == 2) ? -20 : -12;
                olhandoDireita = false;
                estaMovendo = true;
                GerenciadorAudio.tocarSomPassos();
            }
        }

        if (code == Configuracoes.getInstance().getTecla("DIÁRIO")) {
            diarioAberto = !diarioAberto;
            return;
        }

        if (code == Configuracoes.getInstance().getTecla("FALAR")) {
            if (!textoDialogo.isEmpty() && tamanhoTextoVisivel < textoDialogo.length()) {
                tamanhoTextoVisivel = textoDialogo.length();
                tamanhoTextoVisivelAcumulado = textoDialogo.length();
                GerenciadorAudio.pararVozNicolas();
                GerenciadorAudio.pararVozRaquel();
                return;
            }

            if (faseDialogoNicolas == 100 && tamanhoTextoVisivel >= textoDialogo.length()) {
                if (selectedDialogueOption == 0) {
                    frame.mostrarImportadorReal();
                } else {
                    estaEmDialogoNicolas = false;
                    textoDialogo = "";
                    nomePersonagem = "";
                    faseDialogoNicolas = 0;
                    ep1_entregouDesenho = false; // Permite falar de novo para tentar entregar depois
                }
                repaint();
                return;
            }

            if (indiceMapa == 2) {

                // --- Diálogo automático de intro (avançado pela tecla F) ---
                if (aguardandoAvanceSalaAuto && !dialogoSalaAutoConcluido) {
                    GerenciadorAudio.tocarSomDialogo();
                    faseDialogoSalaAuto++;
                    switch (faseDialogoSalaAuto) {
                        case 1:
                            nomePersonagem = "Audrey";
                            textoDialogo = "— Oi, com licença... Sou a Audrey, aluna nova.";
                            break;
                        case 2:
                            nomePersonagem = "Raquel";
                            textoDialogo = "— Ah, oi! Seja bem-vinda! Eu sou a Raquel. Estávamos, justamente, discutindo sobre o projeto do festival cultural.";
                            break;
                        case 3:
                            nomePersonagem = "Nicolas";
                            textoDialogo = "— Salve, Audrey! Sou o Nicollas. Cara, a Raquel quer ir pelo caminho mais clássico, mas, eu acho, que a gente devia meter uma parada mais urbana... tipo, um grafite expressionista.";
                            break;
                        case 4:
                            nomePersonagem = "Nicolas";
                            textoDialogo = "— Arte tem que ter impacto, sabe? Sentimento, puro sentimento bruto!";
                            break;
                        case 5:
                            nomePersonagem = "Camila";
                            textoDialogo = "— Sou a Camila. E, o Nicollas, esquece que o expressionismo é, justamente, sobre distorcer a realidade para expressar as emoções. Não precisa ser só grafite.";
                            break;
                        case 6:
                            nomePersonagem = "Camila";
                            textoDialogo = "— A arte ganha vida, de verdade, quando o conceito é forte. E você, Audrey? Curte alguma vertente?";
                            break;
                        case 7:
                            nomePersonagem = "Audrey";
                            textoDialogo = "— Eu gosto bastante de desenhar, na verdade. Para mim, a arte ajuda a colocar para fora coisas... coisas que as palavras não dão conta.";
                            break;
                        case 8:
                            nomePersonagem = "Raquel";
                            textoDialogo = "— Sério? Que incrível! Olha, a gente está precisando de ajuda, e de ideias novas para o grupo. Que tal um teste rápido, bem rápido, para ver o seu estilo?";
                            break;
                        case 9:
                            nomePersonagem = "Nicolas";
                            textoDialogo = "— Boa! Vamos ver, então, o seu nível artístico. Quero ver como você interpreta o tema: 'O Sentimento da Solidão Urbana'.";
                            break;
                        case 10:
                            nomePersonagem = "Nicolas";
                            textoDialogo = "— Pode ser algo tecnológico, um cenário bem vazio, ou, quem sabe, só alguém na multidão.";
                            break;
                        case 11:
                            nomePersonagem = "Camila";
                            textoDialogo = "— Mostra para a gente, Audrey, do que você é capaz. Estamos, de verdade, bem curiosos!";
                            break;
                        default:
                            // Fim da intro
                            dialogoSalaAutoConcluido = true;
                            aguardandoAvanceSalaAuto = false;
                            estaEmDialogoNicolas = false;
                            textoDialogo = "";
                            nomePersonagem = "";
                            ep1_solidaoUrbanaAtiva = true; // Ativa a nova missão
                            entregarXpPendente();

                            // Exibir notificação na tela
                            String teclaDiario = Configuracoes.getInstance().getNomeTecla("DIÁRIO");
                            textoNotificacao = "Missão adicionada ao Diário! Pressione [" + teclaDiario
                                    + "] para abrir o diário";
                            mostrarNotificacaoMissao = true;
                            contadorNotificacaoMissao = 0;
                            break;
                    }
                    if ("Nicolas".equals(nomePersonagem)) {
                        GerenciadorAudio.tocarVozNicolas();
                    } else {
                        GerenciadorAudio.pararVozNicolas();
                    }
                    return;
                }

                // --- Diálogos pós-intro: Avaliação do Desenho ou Lembrete ---
                if (dialogoSalaAutoConcluido) {
                    estaEmDialogoNicolas = true;

                    if (ep1_solidaoUrbanaAtiva && !ep1_solidaoUrbanaConcluida && !ep1_entregouDesenho) {
                        // Inicia sequência de diálogo de entrega
                        nomePersonagem = "Audrey";
                        textoDialogo = "— Pronto, pessoal. Terminei, finalmente, o esboço do tema que vocês pediram. O que acham?";
                        ep1_entregouDesenho = true;
                        faseDialogoNicolas = 100; // marcador para fluxo de entrega
                    } else if (faseDialogoNicolas == 101) {
                        nomePersonagem = "Raquel";
                        textoDialogo = "— Perfeito! Você passou no teste com folga, Audrey. Agora, você é, oficialmente, a mente criativa do nosso grupo. Pronta para os próximos desafios?";
                        faseDialogoNicolas = 102;
                    } else if (faseDialogoNicolas == 102) {
                        // Fim do Episódio 1! Fade out e save
                        estaEmDialogoNicolas = false;
                        textoDialogo = "";
                        nomePersonagem = "";
                        faseDialogoNicolas = 0;

                        // Dar XP pela conclusão da missão
                        ganharXP(10);
                    } else {
                        // Lembrete se a missão não foi concluída no diário
                        if (Math.abs(audreyX - 250) < 150) {
                            nomePersonagem = "Raquel";
                            textoDialogo = ep1_entregouDesenho ? "— Pronta para os próximos desafios?" : "— Como está ficando o esboço?";
                        } else if (Math.abs(audreyX - 390) < 150) {
                            nomePersonagem = "Nicolas";
                            textoDialogo = ep1_entregouDesenho ? "— Você desenha muito bem!" : "— Mal posso esperar para ver seu desenho!";
                        } else if (Math.abs(audreyX - 530) < 150) {
                            nomePersonagem = "Camila";
                            textoDialogo = ep1_entregouDesenho ? "— Que traço sensacional!" : "— Como está ficando o esboço?";
                        }
                    }
                    if ("Nicolas".equals(nomePersonagem)) {
                        GerenciadorAudio.tocarVozNicolas();
                    } else {
                        GerenciadorAudio.pararVozNicolas();
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
                if (estaEmDialogoNicolas || !textoDialogo.isEmpty()) {
                    return; // Impede saída durante diálogo
                }
                if (ep1_solidaoUrbanaAtiva && !ep1_entregouDesenho) {
                    GerenciadorAudio.tocarSomErro();
                    estaEmDialogoNicolas = true;
                    nomePersonagem = "Audrey";
                    textoDialogo = "Não posso sair sem antes entregar o desenho para o pessoal.";
                    return;
                }
                GerenciadorAudio.tocarSomAbrirPorta();
                indiceMapa = 1;
                audreyX = ultimaPosAoEntraSala;
                textoDialogo = "";
                nomePersonagem = "";
                estaEmDialogoNicolas = false;
                GerenciadorAudio.pararVozNicolas();
                faseDialogoNicolas = 0;
            } // Entrar na sala de aula
            else if (indiceMapa == 1 && Math.abs(audreyX - posPortaX) < 150) {
                if (!sala1Aberta) {
                    GerenciadorAudio.tocarSomErro();
                    estaEmDialogoNicolas = true;
                    nomePersonagem = "Audrey";
                    textoDialogo = "Ainda preciso conhecer o lugar e falar com o pessoal antes de entrar.";
                    return;
                }
                boolean exploracaoCompleta = visitouGinasio && visitouCorredor2 && visitouCorredor3 && visitouCorredor4 && visitouBiblioteca;
                if (exploracaoCompleta) {
                    explorouCorredor = true;
                    GerenciadorAudio.tocarSomAbrirPorta();
                    ultimaPosAoEntraSala = audreyX;
                    indiceMapa = 2;
                    audreyX = 100;
                    textoDialogo = "";
                    nomePersonagem = "";
                    estaEmDialogoNicolas = false;
                    GerenciadorAudio.pararVozNicolas();
                    faseDialogoNicolas = 0;
                } else {
                    nomePersonagem = "Audrey";
                    textoDialogo = "Acho melhor eu ir explorar um pouco a escola antes de entrar na sala de aula.";
                    estaEmDialogoNicolas = true;
                }
            } // Gabi no corredor (X=500, longe da porta em X=350)
            else if (indiceMapa == 1 && Math.abs(audreyX - 500) < 150 && textoDialogo.isEmpty()) {
                if (!ep1FalouGabiCorredor) {
                    GerenciadorAudio.tocarSomDialogo();
                    estaEmDialogoNicolas = true;
                    nomePersonagem = "Gabi";
                    textoDialogo = "Oi! Você é a aluna nova, né? Eu sou a Gabi! A sala de aula é bem ali, pode ir lá tranquila.";
                    ep1FalouGabiCorredor = true;
                    adicionarXpPendente(1);
                    return;
                } else {
                    estaEmDialogoNicolas = true;
                    nomePersonagem = "Gabi";
                    textoDialogo = "Estou passando pelo corredor. Qualquer coisa é só chamar!";
                    return;
                }
            } // Sair da sala de aula
            else if (indiceMapa == 2 && Math.abs(audreyX - 50) < 150) {
                if (estaEmDialogoNicolas || !textoDialogo.isEmpty()) {
                    return; // Bloqueado de sair enquanto estiver em diálogo
                }
                GerenciadorAudio.tocarSomAbrirPorta();
                entregarXpPendente();
                indiceMapa = 1;
                audreyX = ultimaPosAoEntraSala;
            } // Sair da biblioteca
            else if (indiceMapa == 6 && Math.abs(audreyX - 50) < 150) {
                GerenciadorAudio.tocarSomAbrirPorta();
                entregarXpPendente();
                indiceMapa = 3;
                audreyX = 750;
            } // Entrar na biblioteca
            else if (indiceMapa == 3 && Math.abs(audreyX - 750) < 150) {
                GerenciadorAudio.tocarSomAbrirPorta();
                indiceMapa = 6;
                audreyX = 100;
                visitouBiblioteca = true;
                if (!ep1InteragiuBiblioteca) {
                    ep1InteragiuBiblioteca = true;
                    checarObjetivosEp1();
                }
            } // Gabi na biblioteca
            else if (indiceMapa == 6 && personagensNaBiblioteca && Math.abs(audreyX - 550) < 150
                    && textoDialogo.isEmpty()) {
                if (!ep1FalouNpc2) {
                    GerenciadorAudio.tocarSomDialogo();
                    estaEmDialogoNicolas = true;
                    nomePersonagem = "Gabi";
                    textoDialogo = "Ah, você veio! A Ivi e eu estamos aqui na biblioteca agora. A sala de aula é logo ali!";
                    ep1FalouNpc2 = true;
                    adicionarXpPendente(1);
                    checarObjetivosEp1();
                    return;
                } else {
                    estaEmDialogoNicolas = true;
                    nomePersonagem = "Gabi";
                    textoDialogo = "Estamos aqui na biblioteca estudando. Qualquer coisa é só chamar!";
                    return;
                }
            } // Ivi na biblioteca
            else if (indiceMapa == 6 && personagensNaBiblioteca && Math.abs(audreyX - 300) < 150
                    && textoDialogo.isEmpty()) {
                if (!ep1FalouNpc3) {
                    GerenciadorAudio.tocarSomDialogo();
                    estaEmDialogoNicolas = true;
                    nomePersonagem = "Ivi";
                    textoDialogo = "Oi Audrey! A Gabi e eu viemos pra biblioteca estudar. Já foi na sala de aula?";
                    ep1FalouNpc3 = true;
                    adicionarXpPendente(1);
                    checarObjetivosEp1();
                    return;
                } else {
                    estaEmDialogoNicolas = true;
                    nomePersonagem = "Ivi";
                    textoDialogo = "Tem uns livros muito bons aqui! Vem conferir depois.";
                    return;
                }
            } // Mural
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
            } // Porta Trancada
            else if (indiceMapa == 3 && Math.abs(audreyX - 350) < 150 && textoDialogo.isEmpty()) {
                GerenciadorAudio.tocarSomErro();
                nomePersonagem = "Audrey";
                textoDialogo = "A porta está trancada. Parece ser a sala da coordenação.";
            } // Abrir armário
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
                GerenciadorAudio.pararVozNicolas();
            }
        }

        if (code == Configuracoes.getInstance().getTecla("FECHAR")) {
            if (armarioEstaAberto) {
                armarioEstaAberto = false;
            }
            entregarXpPendente();
            textoDialogo = "";
            nomePersonagem = "";
            estaEmDialogoNicolas = false;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        velX = 0;
        estaMovendo = false;
        GerenciadorAudio.pararSomPassos();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    private void ganharXP(int quantidade) {
        xp += quantidade;
        xpGanho = quantidade;
        mostrarNotificacaoXP = true;
        contadorNotificacaoXP = 0;
    }

    private void adicionarXpPendente(int quantidade) {
        xpPendente += quantidade;
        temXpPendente = true;
    }

    private void entregarXpPendente() {
        if (temXpPendente && xpPendente > 0) {
            ganharXP(xpPendente);
            xpPendente = 0;
            temXpPendente = false;
        }
    }

    public void continuarDialogoDesenho() {
        if (faseDialogoNicolas == 100) {
            faseDialogoNicolas = 101;
            estaEmDialogoNicolas = true;
            nomePersonagem = "Nicolas";
            textoDialogo = "\u2014 Ficou irado! Essa perspectiva, cara, deu um peso enorme para o desenho. Voc\u00ea tem, de fato, muita t\u00e9cnica.";
            tamanhoTextoVisivel = 0;
            tamanhoTextoVisivelAcumulado = 0.0;
            GerenciadorAudio.tocarVozNicolas();
            
            // Marcar a missão "Solidão Urbana" como concluída e mostrar animação
            ep1_solidaoUrbanaConcluida = true;
            mostrarAnimacaoRiscado = true;
            contadorAnimacaoRiscado = 0;
            textoMissaoRiscada = "Desenhar 'A Solidão Urbana'";
            GerenciadorAudio.tocarSomColeta();
            
            repaint();
        }
    }

    private void desenharTextoEnvolvido(Graphics2D g2d, String texto, int x, int y, int maxWidth, int lineHeight) {
        FontMetrics fm = g2d.getFontMetrics();
        StringBuilder linha = new StringBuilder();
        int cy = y;
        for (String palavra : texto.split(" ")) {
            String teste = linha.length() == 0 ? palavra : linha.toString() + " " + palavra;
            if (fm.stringWidth(teste) <= maxWidth) {
                linha = new StringBuilder(teste);
            } else {
                g2d.drawString(linha.toString(), x, cy);
                linha = new StringBuilder(palavra);
                cy += lineHeight;
            }
        }
        if (linha.length() > 0) {
            g2d.drawString(linha.toString(), x, cy);
        }
    }

    private void desenharDiario(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, LARGURA, ALTURA);

        int diarioX = 100;
        int diarioY = 50;
        int diarioW = 800;
        int diarioH = 650;

        // Fundo do diario
        g2d.setColor(new Color(255, 250, 240));
        g2d.fillRoundRect(diarioX, diarioY, diarioW, diarioH, 30, 30);
        g2d.setColor(new Color(200, 180, 150));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRoundRect(diarioX, diarioY, diarioW, diarioH, 30, 30);
        g2d.drawLine(diarioX + diarioW / 2, diarioY, diarioX + diarioW / 2, diarioY + diarioH);

        g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 40));
        g2d.setColor(new Color(100, 50, 50));
        g2d.drawString("Meu Diário", diarioX + 80, diarioY + 60);

        Shape clipOriginal = g2d.getClip();
        g2d.setClip(diarioX + 6, diarioY + 6, diarioW / 2 - 12, diarioH - 12);

        g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.PLAIN, 16));

        int textX = diarioX + 40;
        int textY = diarioY + 120;

        g2d.drawString("Leitura (Nível " + nivelLeitura + ")", textX, textY);
        if (missaoLeituraAtiva && !missaoLeituraConcluida) {
            g2d.setColor(Color.RED);
            desenharTextoEnvolvido(g2d, "Ler um livro na VIDA REAL", textX + 20, textY + 30, 270, 24);
            g2d.drawRect(textX + 300, textY + 5, 30, 30); // Checkbox
            g2d.setColor(new Color(100, 50, 50));
        } else if (missaoLeituraConcluida) {
            g2d.setColor(new Color(50, 150, 50));
            desenharTextoEnvolvido(g2d, "Ler um livro (Concluido!)", textX + 20, textY + 30, 270, 24);
            g2d.setColor(new Color(100, 50, 50));
        } else {
            desenharTextoEnvolvido(g2d, "Fale com o grupo de Leitura", textX + 20, textY + 30, 270, 24);
        }

        textY += 100;
        g2d.drawString("Arte (Nível " + nivelArte + ")", textX, textY);
        if (missaoArteAtiva && !missaoArteConcluida) {
            g2d.setColor(Color.RED);
            desenharTextoEnvolvido(g2d, "Recriar obra na VIDA REAL", textX + 20, textY + 30, 270, 24);
            g2d.drawRect(textX + 300, textY + 5, 30, 30);
            g2d.setColor(new Color(100, 50, 50));
        } else if (missaoArteConcluida) {
            g2d.setColor(new Color(50, 150, 50));
            desenharTextoEnvolvido(g2d, "Recriar obra (Concluido!)", textX + 20, textY + 30, 270, 24);
            g2d.setColor(new Color(100, 50, 50));
        } else {
            desenharTextoEnvolvido(g2d, "Fale com o grupo de Arte", textX + 20, textY + 30, 270, 24);
        }

        textY += 100;
        g2d.drawString("Fitness (Nível " + nivelFitness + ")", textX, textY);
        if (missaoFitnessAtiva && !missaoFitnessConcluida) {
            g2d.setColor(Color.RED);
            desenharTextoEnvolvido(g2d, "12 polichinelos na VIDA REAL", textX + 20, textY + 30, 270, 24);
            g2d.drawRect(textX + 300, textY + 5, 30, 30);
            g2d.setColor(new Color(100, 50, 50));
        } else if (missaoFitnessConcluida) {
            g2d.setColor(new Color(50, 150, 50));
            desenharTextoEnvolvido(g2d, "12 polichinelos (Concluido!)", textX + 20, textY + 30, 270, 24);
            g2d.setColor(new Color(100, 50, 50));
        } else {
            desenharTextoEnvolvido(g2d, "Fale com o grupo Fitness", textX + 20, textY + 30, 270, 24);
        }

        // Missao Especial Episodio 1: Solidao Urbana
        textY += 100;
        g2d.drawString("Episódio 1: Missão Especial", textX, textY);
        if (ep1_solidaoUrbanaAtiva && !ep1_solidaoUrbanaConcluida) {
            g2d.setColor(Color.RED);
            desenharTextoEnvolvido(g2d, "Desenhar 'A Solidão Urbana' no mundo real", textX + 20, textY + 30, 270, 24);
            g2d.setColor(new Color(100, 50, 50));
        } else if (ep1_solidaoUrbanaConcluida) {
            g2d.setColor(new Color(50, 150, 50));
            desenharTextoEnvolvido(g2d, "Desenhar 'A Solidão Urbana' (Concluido!)", textX + 20, textY + 30, 270, 24);
            g2d.setColor(new Color(100, 50, 50));
        } else {
            desenharTextoEnvolvido(g2d, "Fale com os professores na Sala 1", textX + 20, textY + 30, 270, 24);
        }

        // Nota inferior (página esquerda)
        desenharTextoEnvolvido(g2d, "Marque a caixa só DEPOIS de concluir na vida real!", diarioX + 40,
                diarioY + diarioH - 50, 340, 24);

        g2d.setClip(clipOriginal);

        // Página direita
        g2d.setClip(diarioX + diarioW / 2 + 6, diarioY + 6, diarioW / 2 - 12, diarioH - 12);

        // Custom missions
        int rightX = diarioX + diarioW / 2 + 40;
        int rightY = diarioY + 80;
        g2d.drawString("Missões Livres (Level 3)", rightX, rightY);

        if (missaoLeituraConcluida && missaoArteConcluida && missaoFitnessConcluida) {
            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.PLAIN, 16));
            g2d.drawString("Agora você cria suas próprias missões!", rightX, rightY + 30);

            if (nivelLeitura < 3) {
                g2d.setColor(Color.BLUE);
                g2d.drawString("Criar e Concluir Missao Leitura", rightX, rightY + 80);
                g2d.drawRect(rightX + 280, rightY + 60, 30, 30);
            } else {
                g2d.setColor(new Color(50, 150, 50));
                g2d.drawString("Leitura Level 3 Atingido!", rightX, rightY + 80);
            }

            if (nivelArte < 3) {
                g2d.setColor(Color.BLUE);
                g2d.drawString("Criar e Concluir Missao Arte", rightX, rightY + 130);
                g2d.drawRect(rightX + 280, rightY + 110, 30, 30);
            } else {
                g2d.setColor(new Color(50, 150, 50));
                g2d.drawString("Arte Level 3 Atingido!", rightX, rightY + 130);
            }

            if (nivelFitness < 3) {
                g2d.setColor(Color.BLUE);
                g2d.drawString("Criar e Concluir Missao Fitness", rightX, rightY + 180);
                g2d.drawRect(rightX + 280, rightY + 160, 30, 30);
            } else {
                g2d.setColor(new Color(50, 150, 50));
                g2d.drawString("Fitness Level 3 Atingido!", rightX, rightY + 180);
            }
        } else {
            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.PLAIN, 16));
            g2d.drawString("(Complete as missões iniciais primeiro)", rightX, rightY + 30);
        }

        g2d.setColor(new Color(100, 50, 50));
        g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.PLAIN, 16));
        String fechar = "Pressione [" + Configuracoes.getInstance().getNomeTecla("DIÁRIO") + "] para fechar";
        FontMetrics fmF = g2d.getFontMetrics();
        g2d.drawString(fechar, rightX + (400 - fmF.stringWidth(fechar)) / 2, diarioY + diarioH - 20);

        g2d.setClip(clipOriginal);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!diarioAberto) {
            if (!armarioEstaAberto && estaEmDialogoNicolas && faseDialogoNicolas == 100 && tamanhoTextoVisivel >= textoDialogo.length()) {
                int w = getWidth();
                int h = getHeight();
                double scaleX = (double) w / LARGURA;
                double scaleY = (double) h / ALTURA;
                double scale = Math.min(scaleX, scaleY);
                int xOffset = (int) ((w - (LARGURA * scale)) / 2);
                int yOffset = (int) ((h - (ALTURA * scale)) / 2);

                int realX = (int) ((e.getX() - xOffset) / scale);
                int realY = (int) ((e.getY() - yOffset) / scale);

                int caixaW = 860;
                int caixaH = 130;
                int caixaX = (LARGURA - caixaW) / 2;
                int caixaY = ALTURA - caixaH - 30;

                Image portraitImg = imgPortraitAudrey;
                int textXOffset = caixaX + 30;
                if (portraitImg != null) {
                    int polaroidW = 160;
                    textXOffset = caixaX + 15 + polaroidW + 25;
                }

                if (realX >= textXOffset - 20 && realX <= caixaX + caixaW - 20) {
                    if (realY >= caixaY + 50 && realY <= caixaY + 80) {
                        selectedDialogueOption = 0;
                        repaint();
                        frame.mostrarImportadorReal();
                        return;
                    } else if (realY >= caixaY + 81 && realY <= caixaY + 115) {
                        selectedDialogueOption = 1;
                        repaint();
                        estaEmDialogoNicolas = false;
                        textoDialogo = "";
                        nomePersonagem = "";
                        faseDialogoNicolas = 0;
                        ep1_entregouDesenho = false;
                        return;
                    }
                }
            }
            return;
        }

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
                ganharXP(10);
            }
        }
        // Arte Level 2
        if (missaoArteAtiva && !missaoArteConcluida) {
            if (realX >= textX + 300 && realX <= textX + 330 && realY >= diarioY + 220 + 5
                    && realY <= diarioY + 220 + 35) {
                missaoArteConcluida = true;
                nivelArte = 2;
                GerenciadorAudio.tocarSomColeta();
                ganharXP(10);
            }
        }
        // Fitness Level 2
        if (missaoFitnessAtiva && !missaoFitnessConcluida) {
            if (realX >= textX + 300 && realX <= textX + 330 && realY >= diarioY + 320 + 5
                    && realY <= diarioY + 320 + 35) {
                missaoFitnessConcluida = true;
                nivelFitness = 2;
                GerenciadorAudio.tocarSomColeta();
                ganharXP(10);
            }
        }
        // Solidao Urbana Episodio 1
        // (A missão só é concluída ao enviar a imagem, marcação manual removida)

        // Missões Level 3
        if (missaoLeituraConcluida && missaoArteConcluida && missaoFitnessConcluida) {
            int rightX = diarioX + 800 / 2 + 40;
            int rightY = diarioY + 80;

            if (nivelLeitura < 3) {
                if (realX >= rightX + 280 && realX <= rightX + 310 && realY >= rightY + 60 && realY <= rightY + 90) {
                    nivelLeitura = 3;
                    GerenciadorAudio.tocarSomColeta();
                    ganharXP(10);
                }
            }
            if (nivelArte < 3) {
                if (realX >= rightX + 280 && realX <= rightX + 310 && realY >= rightY + 110 && realY <= rightY + 140) {
                    nivelArte = 3;
                    GerenciadorAudio.tocarSomColeta();
                    ganharXP(10);
                }
            }
            if (nivelFitness < 3) {
                if (realX >= rightX + 280 && realX <= rightX + 310 && realY >= rightY + 160 && realY <= rightY + 190) {
                    nivelFitness = 3;
                    GerenciadorAudio.tocarSomColeta();
                    ganharXP(10);
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
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File(JogoAudrey.resolvePath("CrayonHandRegular2016Demo.ttf")))
                    .deriveFont(28f);
            fontTitulo = Font.createFont(Font.TRUETYPE_FONT, new java.io.File(JogoAudrey.resolvePath("KGSecondChancesSketch.ttf")))
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

        JButton[] btns = {btnSlot1, btnSlot2, btnSlot3};
        for (int i = 0; i < btns.length; i++) {
            if (btns[i] != null) {
                btns[i].setBounds(bx, startY + i * gap, bw, bh);
                if (btnApagarSlots[i] != null) {
                    btnApagarSlots[i].setBounds(bx + bw + 10, startY + i * gap, bh, bh); // lixeira ao lado, do tamanho
                    // da altura do botao
                }
            }
        }
        if (btnVoltar != null) {
            btnVoltar.setBounds((w - 400) / 2, startY + 3 * gap + 20, 400, 70); // voltar em baixo
        }
    }

    private JButton criarBotaoLixeira(int slot) {
        JButton botao = new JButton("🗑") {
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
            boolean confirm = frame.mostrarConfirmacao(
                    "Confirmar Exclusão", "Deseja REALMENTE apagar o save do Slot " + slot + "?\nEsta ação não pode ser desfeita.");
            if (confirm) {
                Database.apagarSave(slot);
                frame.mostrarMensagem("Aviso", "Save apagado com sucesso.");
                atualizarBotoes();
                if (frame.getSlotAtual() == slot) {
                    frame.setSlotAtual(-1);
                }
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
        JButton[] botoes = {btnSlot1, btnSlot2, btnSlot3};
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
                btnApagarSlots[i].setVisible(existe); // So mostra lixeira se existir save nesse slot
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
                    boolean confirm = frame.mostrarConfirmacao(
                            "Aviso",
                            "O Slot " + slot + " já possui um save.\nDeseja realmente apagar e iniciar um novo jogo?");
                    if (!confirm) {
                        return;
                    }
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
                    boolean confirm = frame.mostrarConfirmacao(
                            "Confirmar Exclusão",
                            "ATENÇÃO: Deseja REALMENTE apagar o save do Slot " + slot
                            + "?\nEsta ação não pode ser desfeita.");
                    if (confirm) {
                        Database.apagarSave(slot);
                        frame.mostrarMensagem("Aviso", "Save apagado com sucesso.");
                        atualizarBotoes();
                    }
                }
                break;

            case JogoAudrey.ACAO_SALVAR:
                if (ocupado && slot != frame.getSlotAtual()) {
                    boolean confirm = frame.mostrarConfirmacao(
                            "Aviso", "O Slot " + slot + " já possui um save.\nDeseja realmente sobrescrevê-lo?");
                    if (!confirm) {
                        return;
                    }
                }
                frame.setSlotAtual(slot);
                frame.getJogoPanel().salvarEstado(slot);
                frame.mostrarMensagem("Sucesso", "Jogo salvo com sucesso no Slot " + slot + "!");
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

        int w = getWidth(), h = getHeight();
        GradientPaint gradient = new GradientPaint(0, 0, new Color(30, 20, 50), w, h, new Color(20, 10, 40));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, w, h);

        Image banner = frame.getMenuPrincipal().getImgBanner();
        if (banner != null) {
            g2d.drawImage(banner, 0, 0, w, h, this);
        }

        GradientPaint overlay = new GradientPaint(0, 0, new Color(0, 0, 0, 180), 0, h / 2f, new Color(0, 0, 0, 0));
        g2d.setPaint(overlay);
        g2d.fillRect(0, 0, w, h / 2);

        g2d.setFont(fontTitulo);
        String titulo = "";
        if (acaoAtual == JogoAudrey.ACAO_NOVO) {
            titulo = "NOVO JOGO";
        } else if (acaoAtual == JogoAudrey.ACAO_CONTINUAR) {
            titulo = "CONTINUAR";
        } else if (acaoAtual == JogoAudrey.ACAO_APAGAR) {
            titulo = "APAGAR SAVE";
        } else if (acaoAtual == JogoAudrey.ACAO_SALVAR) {
            titulo = "SALVAR JOGO";
        }

        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(titulo)) / 2;

        g2d.setColor(new Color(150, 130, 230, 140)); // Sombra lavanda
        g2d.drawString(titulo, x + 4, 124);

        GradientPaint titleGrad = new GradientPaint(
                x, 70, new Color(220, 195, 255), // Lavanda claro
                x, 130, new Color(150, 130, 230) // Lavanda
        );
        g2d.setPaint(titleGrad);
        g2d.drawString(titulo, x, 120);
    }
}

class Configuracoes {

    private static Configuracoes instancia;
    private java.util.HashMap<String, Integer> teclas;
    private int volumeMusica = 50;
    private int volumeEfeitos = 50;
    private int brilho = 50;
    private boolean shaderAtivo = false;

    // Parâmetros do Shader Sunset
    private int shaderRayStrength = 45;
    private int shaderBloom = 60;
    private int shaderFog = 25;
    private int shaderWarmth = 80;
    private int shaderVignette = 65;

    public static final String[] ACOES = {
        "ESQUERDA", "DIREITA", "INTERAGIR", "FALAR",
        "FECHAR", "INVENTÁRIO", "OBJETIVOS", "DIÁRIO", "MENU"
    };
    public static final String[] LABELS = {
        "Mover Esquerda", "Mover Direita", "Interagir", "Falar",
        "Fechar Diálogo", "Inventário", "Objetivos", "Diário", "Menu"
    };

    private Configuracoes() {
        teclas = new java.util.HashMap<>();
        resetarTeclas();
        carregarConfiguracoes();
    }

    public static Configuracoes getInstance() {
        if (instancia == null) {
            instancia = new Configuracoes();
        }
        return instancia;
    }

    public void resetarTeclas() {
        teclas.put("ESQUERDA", KeyEvent.VK_A);
        teclas.put("DIREITA", KeyEvent.VK_D);
        teclas.put("INTERAGIR", KeyEvent.VK_E);
        teclas.put("FALAR", KeyEvent.VK_F);
        teclas.put("FECHAR", KeyEvent.VK_Q);
        teclas.put("INVENTÁRIO", KeyEvent.VK_B);
        teclas.put("OBJETIVOS", KeyEvent.VK_M);
        teclas.put("DIÁRIO", KeyEvent.VK_J);
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

    public int getVolumeMusica() {
        return volumeMusica;
    }

    public void setVolumeMusica(int v) {
        volumeMusica = Math.max(0, Math.min(100, v));
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

    public int getShaderRayStrength() {
        return shaderRayStrength;
    }

    public void setShaderRayStrength(int v) {
        shaderRayStrength = Math.max(0, Math.min(100, v));
        PastelShader.rayStrength = shaderRayStrength;
    }

    public int getShaderBloom() {
        return shaderBloom;
    }

    public void setShaderBloom(int v) {
        shaderBloom = Math.max(0, Math.min(100, v));
        PastelShader.bloom = shaderBloom;
    }

    public int getShaderFog() {
        return shaderFog;
    }

    public void setShaderFog(int v) {
        shaderFog = Math.max(0, Math.min(100, v));
        PastelShader.fog = shaderFog;
    }

    public int getShaderWarmth() {
        return shaderWarmth;
    }

    public void setShaderWarmth(int v) {
        shaderWarmth = Math.max(0, Math.min(100, v));
        PastelShader.warmth = shaderWarmth;
    }

    public int getShaderVignette() {
        return shaderVignette;
    }

    public void setShaderVignette(int v) {
        shaderVignette = Math.max(0, Math.min(100, v));
        PastelShader.vignette = shaderVignette;
    }

    /**
     * Sincroniza todos os parâmetros com PastelShader (chamar após carregar
     * config)
     */
    public void syncShaderParams() {
        PastelShader.rayStrength = shaderRayStrength;
        PastelShader.bloom = shaderBloom;
        PastelShader.fog = shaderFog;
        PastelShader.warmth = shaderWarmth;
        PastelShader.vignette = shaderVignette;
    }

    public void salvarConfiguracoes() {
        java.util.Properties props = new java.util.Properties();
        for (java.util.Map.Entry<String, Integer> entry : teclas.entrySet()) {
            props.setProperty("tecla_" + entry.getKey(), String.valueOf(entry.getValue()));
        }
        props.setProperty("volumeMusica", String.valueOf(volumeMusica));
        props.setProperty("volumeEfeitos", String.valueOf(volumeEfeitos));
        props.setProperty("brilho", String.valueOf(brilho));
        props.setProperty("shaderAtivo", String.valueOf(shaderAtivo));
        props.setProperty("shaderRayStrength", String.valueOf(shaderRayStrength));
        props.setProperty("shaderBloom", String.valueOf(shaderBloom));
        props.setProperty("shaderFog", String.valueOf(shaderFog));
        props.setProperty("shaderWarmth", String.valueOf(shaderWarmth));
        props.setProperty("shaderVignette", String.valueOf(shaderVignette));

        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(JogoAudrey.resolvePath("config.properties"))) {
            props.store(fos, "Configuracoes do Jogo");
        } catch (Exception e) {
            System.err.println("Erro ao salvar config: " + e.getMessage());
        }
    }

    public void carregarConfiguracoes() {
        java.util.Properties props = new java.util.Properties();
        try (java.io.FileInputStream fis = new java.io.FileInputStream(JogoAudrey.resolvePath("config.properties"))) {
            props.load(fis);

            for (String acao : ACOES) {
                if (props.containsKey("tecla_" + acao)) {
                    teclas.put(acao, Integer.parseInt(props.getProperty("tecla_" + acao)));
                }
            }
            if (props.containsKey("volumeMusica")) {
                volumeMusica = Integer.parseInt(props.getProperty("volumeMusica"));
            }
            if (props.containsKey("volumeEfeitos")) {
                volumeEfeitos = Integer.parseInt(props.getProperty("volumeEfeitos"));
            }
            if (props.containsKey("brilho")) {
                brilho = Integer.parseInt(props.getProperty("brilho"));
            }
            if (props.containsKey("shaderAtivo")) {
                shaderAtivo = Boolean.parseBoolean(props.getProperty("shaderAtivo"));
            }
            if (props.containsKey("shaderRayStrength")) {
                shaderRayStrength = Integer.parseInt(props.getProperty("shaderRayStrength"));
            }
            if (props.containsKey("shaderBloom")) {
                shaderBloom = Integer.parseInt(props.getProperty("shaderBloom"));
            }
            if (props.containsKey("shaderFog")) {
                shaderFog = Integer.parseInt(props.getProperty("shaderFog"));
            }
            if (props.containsKey("shaderWarmth")) {
                shaderWarmth = Integer.parseInt(props.getProperty("shaderWarmth"));
            }
            if (props.containsKey("shaderVignette")) {
                shaderVignette = Integer.parseInt(props.getProperty("shaderVignette"));
            }
            syncShaderParams();

        } catch (Exception e) {
            System.err.println("Configuracoes não encontradas ou erro ao carregar: " + e.getMessage());
        }
    }
}

class ConfiguracoesPanel extends JPanel implements KeyListener {

    private JogoAudrey frame;
    private String origem = "menuPrincipal";
    private Font fontCrayonHand, fontTitulo;
    private int abaAtual = 0;

    private JButton btnAbaControle, btnAbaSom, btnAbaTela;
    private JButton btnVoltar, btnResetar, btnSalvar;
    private BotaoEstilizado btnShader;

    private JButton[] btnTeclas;
    private int esperandoTeclaIndex = -1;

    private JSlider sliderMusica;
    private JSlider sliderEfeitos;
    private JSlider sliderBrilho;

    private JSlider sliderRay;
    private JSlider sliderBloom;
    private JSlider sliderFog;
    private JSlider sliderWarmth;
    private JSlider sliderVignette;

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
        sliderMusica.setValue(Configuracoes.getInstance().getVolumeMusica());
        sliderEfeitos.setValue(Configuracoes.getInstance().getVolumeEfeitos());
        sliderBrilho.setValue(Configuracoes.getInstance().getBrilho());

        if (sliderRay != null) {
            sliderRay.setValue(Configuracoes.getInstance().getShaderRayStrength());
        }
        if (sliderBloom != null) {
            sliderBloom.setValue(Configuracoes.getInstance().getShaderBloom());
        }
        if (sliderFog != null) {
            sliderFog.setValue(Configuracoes.getInstance().getShaderFog());
        }
        if (sliderWarmth != null) {
            sliderWarmth.setValue(Configuracoes.getInstance().getShaderWarmth());
        }
        if (sliderVignette != null) {
            sliderVignette.setValue(Configuracoes.getInstance().getShaderVignette());
        }

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
        btnAbaControle = new BotaoEstilizado("CONTROLE", JogoAudrey.getCachedFont(fontCrayonHand, 20f));
        btnAbaControle.addActionListener(e -> {
            abaAtual = 0;
            esperandoTeclaIndex = -1;
            atualizarAba();
        });
        add(btnAbaControle);

        btnAbaSom = new BotaoEstilizado("SOM", JogoAudrey.getCachedFont(fontCrayonHand, 20f));
        btnAbaSom.addActionListener(e -> {
            abaAtual = 1;
            esperandoTeclaIndex = -1;
            atualizarAba();
        });
        add(btnAbaSom);

        btnAbaTela = new BotaoEstilizado("TELA", JogoAudrey.getCachedFont(fontCrayonHand, 20f));
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
                    JogoAudrey.getCachedFont(fontCrayonHand, 18f));
            btnTeclas[i].addActionListener(e -> {
                esperandoTeclaIndex = idx;
                btnTeclas[idx].setText("...");
                ConfiguracoesPanel.this.requestFocusInWindow();
            });
            add(btnTeclas[i]);
        }

        btnResetar = new BotaoEstilizado("RESETAR PADRAO", JogoAudrey.getCachedFont(fontCrayonHand, 18f));
        btnResetar.addActionListener(e -> {
            Configuracoes.getInstance().resetarTeclas();
            atualizarBotoesTeclas();
            repaint();
        });
        add(btnResetar);

        sliderMusica = criarSliderVolume("volumeMusica", Configuracoes.getInstance().getVolumeMusica(),
                new Color(50, 150, 255), new Color(150, 200, 255),
                new Color(200, 230, 255), new Color(80, 140, 220));
        sliderEfeitos = criarSliderVolume("volumeEfeitos", Configuracoes.getInstance().getVolumeEfeitos(),
                new Color(100, 60, 200), new Color(160, 120, 255),
                new Color(200, 180, 255), new Color(120, 80, 220));

        sliderBrilho = new JSlider(0, 100, Configuracoes.getInstance().getBrilho()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                int trackH = 10, trackY = h / 2 - trackH / 2;
                int filled = (int) ((getValue() / 100.0) * (w - 20));
                // trilha vazia
                g2.setColor(new Color(40, 30, 80));
                g2.fillRoundRect(10, trackY, w - 20, trackH, 8, 8);
                // trilha preenchida (amarelo/laranja para brilho)
                GradientPaint gp = new GradientPaint(10, 0, new Color(180, 120, 20), 10 + filled, 0, new Color(255, 220, 80));
                g2.setPaint(gp);
                g2.fillRoundRect(10, trackY, filled, trackH, 8, 8);
                // thumb
                int tx = 10 + filled - 8;
                g2.setColor(new Color(255, 240, 150));
                g2.fillOval(tx, h / 2 - 10, 18, 18);
                g2.setColor(new Color(200, 160, 40));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(tx, h / 2 - 10, 18, 18);
                g2.dispose();
            }
        };
        sliderBrilho.setOpaque(false);
        sliderBrilho.setUI(new javax.swing.plaf.basic.BasicSliderUI(sliderBrilho) {
            @Override
            public void paintThumb(Graphics g) {
            }

            @Override
            public void paintTrack(Graphics g) {
            }
        });
        sliderBrilho.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                Configuracoes.getInstance().setBrilho(sliderBrilho.getValue());
                repaint();
            }
        });
        add(sliderBrilho);

        btnShader = new BotaoEstilizado(
                "SHADER PASTEL: " + (Configuracoes.getInstance().isShaderAtivo() ? "LIGADO" : "DESLIGADO"),
                JogoAudrey.getCachedFont(fontCrayonHand, 18f));
        btnShader.addActionListener(e -> {
            boolean ativo = !Configuracoes.getInstance().isShaderAtivo();
            Configuracoes.getInstance().setShaderAtivo(ativo);
            btnShader.setText("SHADER PASTEL: " + (ativo ? "LIGADO" : "DESLIGADO"));
            atualizarAba(); // Atualiza visibilidade dos sliders do shader
        });
        add(btnShader);

        // Função auxiliar para criar sliders do shader
        java.util.function.BiFunction<Integer, java.util.function.Consumer<Integer>, JSlider> createShaderSlider = (val, setter) -> {
            JSlider s = new JSlider(0, 100, val) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(), h = getHeight();
                    int trackH = 8, trackY = h / 2 - trackH / 2;
                    int filled = (int) ((getValue() / 100.0) * (w - 20));
                    g2.setColor(new Color(40, 30, 80));
                    g2.fillRoundRect(10, trackY, w - 20, trackH, 8, 8);
                    GradientPaint gp = new GradientPaint(10, 0, new Color(50, 150, 255), 10 + filled, 0, new Color(150, 200, 255));
                    g2.setPaint(gp);
                    g2.fillRoundRect(10, trackY, filled, trackH, 8, 8);
                    int tx = 10 + filled - 8;
                    g2.setColor(new Color(200, 230, 255));
                    g2.fillOval(tx, h / 2 - 8, 16, 16);
                    g2.dispose();
                }
            };
            s.setOpaque(false);
            s.setUI(new javax.swing.plaf.basic.BasicSliderUI(s) {
                @Override
                public void paintThumb(Graphics g) {
                }

                @Override
                public void paintTrack(Graphics g) {
                }
            });
            s.addChangeListener(e -> {
                setter.accept(s.getValue());
                repaint();
            });
            add(s);
            return s;
        };

        sliderRay = createShaderSlider.apply(Configuracoes.getInstance().getShaderRayStrength(), v -> Configuracoes.getInstance().setShaderRayStrength(v));
        sliderBloom = createShaderSlider.apply(Configuracoes.getInstance().getShaderBloom(), v -> Configuracoes.getInstance().setShaderBloom(v));
        sliderFog = createShaderSlider.apply(Configuracoes.getInstance().getShaderFog(), v -> Configuracoes.getInstance().setShaderFog(v));
        sliderWarmth = createShaderSlider.apply(Configuracoes.getInstance().getShaderWarmth(), v -> Configuracoes.getInstance().setShaderWarmth(v));
        sliderVignette = createShaderSlider.apply(Configuracoes.getInstance().getShaderVignette(), v -> Configuracoes.getInstance().setShaderVignette(v));

        btnSalvar = new BotaoEstilizado("SALVAR ALTERAÇÕES", JogoAudrey.getCachedFont(fontCrayonHand, 22f));
        btnSalvar.addActionListener(e -> {
            Configuracoes.getInstance().salvarConfiguracoes();
            GerenciadorAudio.tocarSomColeta();
            btnSalvar.setText("ALTERAÇÕES SALVAS!");
            Timer t = new Timer(1500, evt -> {
                btnSalvar.setText("SALVAR ALTERAÇÕES");
            });
            t.setRepeats(false);
            t.start();
        });
        add(btnSalvar);

        btnVoltar = new BotaoEstilizado("VOLTAR", JogoAudrey.getCachedFont(fontCrayonHand, 22f));
        btnVoltar.addActionListener(e -> {
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

    private JSlider criarSliderVolume(String chave, int valorInicial,
            Color corGradA, Color corGradB, Color corThumb, Color corBorda) {
        JSlider s = new JSlider(0, 100, valorInicial) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                int trackH = 10, trackY = h / 2 - trackH / 2;
                int filled = (int) ((getValue() / 100.0) * (w - 20));
                g2.setColor(new Color(40, 30, 80));
                g2.fillRoundRect(10, trackY, w - 20, trackH, 8, 8);
                GradientPaint gp = new GradientPaint(10, 0, corGradA, 10 + filled, 0, corGradB);
                g2.setPaint(gp);
                g2.fillRoundRect(10, trackY, filled, trackH, 8, 8);
                int tx = 10 + filled - 8;
                g2.setColor(corThumb);
                g2.fillOval(tx, h / 2 - 10, 18, 18);
                g2.setColor(corBorda);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(tx, h / 2 - 10, 18, 18);
                g2.dispose();
            }
        };
        s.setOpaque(false);
        s.setUI(new javax.swing.plaf.basic.BasicSliderUI(s) {
            @Override
            public void paintThumb(Graphics g) {
            }

            @Override
            public void paintTrack(Graphics g) {
            }
        });
        s.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int valor = s.getValue();
                if ("volumeMusica".equals(chave)) {
                    Configuracoes.getInstance().setVolumeMusica(valor);
                    GerenciadorAudio.atualizarVolumeMusica(valor);
                } else {
                    Configuracoes.getInstance().setVolumeEfeitos(valor);
                }
                repaint();
            }
        });
        add(s);
        return s;
    }

    private void atualizarBotoesTeclas() {
        for (int i = 0; i < Configuracoes.ACOES.length; i++) {
            btnTeclas[i].setText(Configuracoes.getInstance().getNomeTecla(Configuracoes.ACOES[i]));
        }
    }

    private void atualizarAba() {
        for (JButton btn : btnTeclas) {
            btn.setVisible(abaAtual == 0);
        }
        btnResetar.setVisible(abaAtual == 0);
        sliderMusica.setVisible(abaAtual == 1);
        sliderEfeitos.setVisible(abaAtual == 1);
        sliderBrilho.setVisible(abaAtual == 2);

        boolean isTela = (abaAtual == 2);
        if (btnShader != null) {
            btnShader.setVisible(isTela);
        }

        boolean showShaderOptions = isTela && Configuracoes.getInstance().isShaderAtivo();
        if (sliderRay != null) {
            sliderRay.setVisible(showShaderOptions);
        }
        if (sliderBloom != null) {
            sliderBloom.setVisible(showShaderOptions);
        }
        if (sliderFog != null) {
            sliderFog.setVisible(showShaderOptions);
        }
        if (sliderWarmth != null) {
            sliderWarmth.setVisible(showShaderOptions);
        }
        if (sliderVignette != null) {
            sliderVignette.setVisible(showShaderOptions);
        }

        revalidate();
        repaint();
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) {
            return;
        }

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
        sliderMusica.setBounds(sliderX, contentY + 60, sliderW, 60);
        sliderEfeitos.setBounds(sliderX, contentY + 170, sliderW, 60);

        // Tela layout
        sliderBrilho.setBounds(sliderX, contentY + 60, sliderW, 60);

        int btnShaderY = contentY + 160;
        if (btnShader != null) {
            btnShader.setBounds(sliderX, btnShaderY, sliderW, 50);
        }

        int shaderSlidersY = btnShaderY + 70;
        int gapShader = 40;
        if (sliderRay != null) {
            sliderRay.setBounds(sliderX, shaderSlidersY + gapShader * 0, sliderW, 30);
        }
        if (sliderBloom != null) {
            sliderBloom.setBounds(sliderX, shaderSlidersY + gapShader * 1, sliderW, 30);
        }
        if (sliderFog != null) {
            sliderFog.setBounds(sliderX, shaderSlidersY + gapShader * 2, sliderW, 30);
        }
        if (sliderWarmth != null) {
            sliderWarmth.setBounds(sliderX, shaderSlidersY + gapShader * 3, sliderW, 30);
        }
        if (sliderVignette != null) {
            sliderVignette.setBounds(sliderX, shaderSlidersY + gapShader * 4, sliderW, 30);
        }

        int btnW_bottom = Math.max(220, w / 4 - 20);
        int btnH_bottom = 55;
        int spacing = 20;
        int totalWidth = btnW_bottom * 2 + spacing;
        int startX = (w - totalWidth) / 2;

        int contentBottom;
        if (abaAtual == 0) {
            contentBottom = contentY + btnTeclas.length * (rowH + gap) + 10 + rowH;
        } else if (abaAtual == 1) {
            contentBottom = contentY + 170 + 60 + 30;
        } else {
            contentBottom = Configuracoes.getInstance().isShaderAtivo() ? (shaderSlidersY + gapShader * 5) : (btnShaderY + 60);
        }
        btnSalvar.setBounds(startX, contentBottom + 25, btnW_bottom, btnH_bottom);
        btnVoltar.setBounds(startX + btnW_bottom + spacing, contentBottom + 25, btnW_bottom, btnH_bottom);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int W = getWidth(), H = getHeight();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(0, 0, new Color(30, 20, 50), W, H, new Color(20, 10, 40));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, W, H);

        Image banner = frame.getMenuPrincipal().getImgBanner();
        if (banner != null) {
            g2d.drawImage(banner, 0, 0, W, H, this);
        }

        GradientPaint overlay = new GradientPaint(0, 0, new Color(0, 0, 0, 180), 0, H / 2f, new Color(0, 0, 0, 0));
        g2d.setPaint(overlay);
        g2d.fillRect(0, 0, W, H / 2);

        int titleY = (int) (H * 0.10);
        g2d.setFont(fontTitulo);
        String titulo = "CONFIGURAÇÕES";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (W - fm.stringWidth(titulo)) / 2;

        g2d.setColor(new Color(150, 130, 230, 140));
        g2d.drawString(titulo, x + 4, titleY + 4);

        GradientPaint titleGrad = new GradientPaint(x, titleY - 30, new Color(220, 195, 255), x, titleY,
                new Color(150, 130, 230));
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

        // Fundo escuro semi-transparente atras do painel de conteudo
        int bgHeight = 0;
        if (abaAtual == 0) {
            bgHeight = Configuracoes.LABELS.length * 51 + 60;
        } else if (abaAtual == 1) {
            bgHeight = 270;
        } else {
            bgHeight = Configuracoes.getInstance().isShaderAtivo() ? 460 : 250;
        }

        g2d.setColor(new Color(20, 15, 35, 200));
        g2d.fillRoundRect(contentX - 20, contentY - 15, contentW + 40, bgHeight, 25, 25);
        g2d.setColor(new Color(100, 80, 160, 150));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(contentX - 20, contentY - 15, contentW + 40, bgHeight, 25, 25);

        if (abaAtual == 0) {
            int rowH = 45;
            int gapRow = 6;
            for (int i = 0; i < Configuracoes.LABELS.length; i++) {
                int textY = contentY + i * (rowH + gapRow) + rowH / 2 + 7;
                g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 20f));
                g2d.setColor(new Color(120, 80, 100));
                g2d.drawString(Configuracoes.LABELS[i], contentX, textY);
            }
            if (esperandoTeclaIndex >= 0) {
                g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.ITALIC, 16f));
                g2d.setColor(new Color(200, 100, 100));
                int msgY = contentY + Configuracoes.LABELS.length * (rowH + gapRow) - 5;
                g2d.drawString("Pressione uma tecla para configurar...", contentX, msgY);
            }
        } else if (abaAtual == 1) {
            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 26f));
            g2d.drawString("Música", contentX, contentY + 40);
            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 20f));
            String musicaText = Configuracoes.getInstance().getVolumeMusica() + "%";
            int musicaTextW = g2d.getFontMetrics().stringWidth(musicaText);
            g2d.drawString(musicaText, (W - musicaTextW) / 2, contentY + 135);

            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 26f));
            g2d.drawString("Efeitos Sonoros", contentX, contentY + 150);
            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 20f));
            String volText = Configuracoes.getInstance().getVolumeEfeitos() + "%";
            int volTextW = g2d.getFontMetrics().stringWidth(volText);
            g2d.drawString(volText, (W - volTextW) / 2, contentY + 245);
        } else if (abaAtual == 2) {
            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, Font.BOLD, 26f));
            g2d.drawString("Brilho da Tela", contentX, contentY + 40);
            g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 20f));
            String brilhoText = Configuracoes.getInstance().getBrilho() + "%";
            int brilhoTextW = g2d.getFontMetrics().stringWidth(brilhoText);
            g2d.drawString(brilhoText, (W - brilhoTextW) / 2, contentY + 140);

            int btnShaderY = contentY + 160;

            if (Configuracoes.getInstance().isShaderAtivo()) {
                int shaderSlidersY = btnShaderY + 70;
                int gapShader = 40;
                g2d.setFont(JogoAudrey.getCachedFont(fontCrayonHand, 16f));

                String[] shaderLabels = {"Intensidade dos raios", "Bloom", "Neblina", "Temperatura de cor", "Vinheta"};
                int[] shaderVals = {
                    Configuracoes.getInstance().getShaderRayStrength(),
                    Configuracoes.getInstance().getShaderBloom(),
                    Configuracoes.getInstance().getShaderFog(),
                    Configuracoes.getInstance().getShaderWarmth(),
                    Configuracoes.getInstance().getShaderVignette()
                };

                for (int i = 0; i < 5; i++) {
                    g2d.setColor(new Color(180, 200, 255));
                    g2d.drawString(shaderLabels[i], contentX, shaderSlidersY + gapShader * i - 5);
                    String valText = shaderVals[i] + "%";
                    int vw = g2d.getFontMetrics().stringWidth(valText);
                    g2d.drawString(valText, contentX + contentW - vw, shaderSlidersY + gapShader * i - 5);
                }
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

    private Color corFundoNormal = new Color(45, 35, 95);
    private Color corFundoHover = new Color(75, 65, 135);
    private Color corFundoPress = new Color(30, 20, 70);
    private Color corBordaNormal = new Color(100, 120, 255);
    private Color corBordaHover = new Color(255, 220, 50);
    private Color corTexto = new Color(255, 255, 255);
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
                if (isEnabled()) {
                    setFont(JogoAudrey.getCachedFont(fonteBase, Font.BOLD));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()) {
                    setFont(fonteBase);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int r = Math.min(w, h) - 10; // Formato pilula (bem arredondado)

        boolean isHovered = getModel().isRollover();
        boolean isPressed = getModel().isPressed();
        boolean isEnabled = isEnabled();

        Color base = !isEnabled ? new Color(40, 30, 60)
                : isPressed ? corFundoPress : isHovered ? corFundoHover : corFundoNormal;
        Color topo = !isEnabled ? new Color(55, 45, 80)
                : isHovered ? new Color(115, 100, 190) : new Color(78, 63, 155);

        int offsetY = isPressed ? 4 : 0;

        // Sombra / Base 3D (dupla para mais profundidade)
        if (!isPressed && isEnabled) {
            g2d.setColor(new Color(10, 5, 20, 110));
            g2d.fillRoundRect(0, 5, w, h - 5, r, r);
            g2d.setColor(new Color(10, 5, 20, 50));
            g2d.fillRoundRect(0, 9, w, h - 9, r, r);
        }

        // Glow externo no hover
        if (isHovered && isEnabled) {
            g2d.setColor(new Color(255, 220, 50, 40));
            g2d.fillRoundRect(-3, offsetY, w + 6, h + 2, r + 6, r + 6);
        }

        // Fundo principal com gradiente vertical
        GradientPaint grad = new GradientPaint(0, offsetY, topo, 0, offsetY + h, base);
        g2d.setPaint(grad);
        g2d.fillRoundRect(0, offsetY, w, h - 5, r, r);

        // Brilho interno no topo (efeito glass)
        if (isEnabled) {
            int topA = isHovered ? 55 : 35;
            GradientPaint glass = new GradientPaint(0, offsetY, new Color(255, 255, 255, topA),
                    0, offsetY + h / 2, new Color(255, 255, 255, 0));
            g2d.setPaint(glass);
            g2d.fillRoundRect(0, offsetY, w, h - 5, r, r);
        }

        // Borda (sombra interna + borda colorida)
        Color corBorda = !isEnabled ? new Color(80, 70, 110)
                : isHovered ? corBordaHover : corBordaNormal;
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.setStroke(new BasicStroke(3f));
        g2d.drawRoundRect(1, offsetY + 1, w - 3, h - 6, r, r);
        g2d.setColor(corBorda);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(1, offsetY, w - 3, h - 6, r, r);

        // Texto com sombra
        FontMetrics fm = g2d.getFontMetrics(getFont());
        int tx = (w - fm.stringWidth(getText())) / 2;
        int ty = offsetY + (h - 5 - fm.getHeight()) / 2 + fm.getAscent();

        g2d.setColor(new Color(0, 0, 0, 90));
        g2d.drawString(getText(), tx + 1, ty + 1);
        g2d.setColor(!isEnabled ? new Color(100, 90, 130) : corTexto);
        g2d.drawString(getText(), tx, ty);

        g2d.dispose();
    }
}


