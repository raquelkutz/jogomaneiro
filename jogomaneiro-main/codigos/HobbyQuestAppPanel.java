import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * HobbyQuestAppPanel — Recriação 100% fiel e idêntica aos screenshots do APK Hobby Quest.
 * Renderiza todas as telas (Login, Home, Objetivos, Desempenho, Ajuda, Perfil) com as cores,
 * texturas, sombras, fitas adesivas (washi tape), alfinetes 3D, badges de XP e ícones oficiais.
 */
public class HobbyQuestAppPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

    private JogoAudrey frame;
    private final int LARGURA = 1000;
    private final int ALTURA = 750;

    // Container dinâmico — proporcional à tela (preenche ~92% da tela cheia)
    private int CONTAINER_W = 950;
    private int getContainerW() {
        int w = getWidth() > 0 ? getWidth() : LARGURA;
        return Math.max(600, (int)(w * 0.92f));
    }

    // Estado de Autenticação
    private boolean estaLogado = false;
    private boolean modoCadastro = false;

    // Telas do App (quando logado):
    // 0=HOME, 1=OBJETIVOS, 2=DESEMPENHO, 3=AJUDA, 4=PERFIL
    private int telaAtual = 0;

    // ===== ANIMAÇÃO DE TRANSIÇÃO =====
    private float transAlpha = 1.0f;       // 1.0 = totalmente visível
    private float transSlideX = 0f;        // offset horizontal para slide
    private int transTelaDestino = -1;     // -1 = sem transição ativa
    private long transInicio = 0;
    private final int TRANS_DURACAO = 280; // ms da animação
    private boolean transindoSaida = true; // true = saindo, false = entrando

    // Animação de clique (scale feedback)
    private float clickScale = 1.0f;
    private long clickInicio = 0;
    private Rectangle clickRect = null;
    private final int CLICK_DURACAO = 150;

    // Fontes
    private Font fontTitulo, fontCabin, fontCaveat, fontWalter;
    private Timer animTimer;
    private float animTicks = 0f;

    // Cores Oficiais — Ajustadas para serem IDÊNTICAS aos prints do APK
    private final Color COLOR_BG_PAGE       = new Color(255, 248, 231);      // Fundo amarelado do caderno
    private final Color COLOR_NOTEBOOK_LINE = new Color(195, 215, 230, 100); // Linhas azuis suaves
    private final Color COLOR_GREEN_TITLE   = new Color(103, 167, 96);       // Verde do título
    private final Color COLOR_DARK_GREEN    = new Color(45, 122, 38);        // Verde escuro ativo
    private final Color COLOR_GOLD_STAR     = new Color(226, 183, 64);       // Estrela dourada
    private final Color COLOR_CARDBOARD     = new Color(229, 217, 197);      // Cartão rasgado
    private final Color COLOR_POSTIT_YELLOW = new Color(252, 226, 139);      // Post-it
    private final Color COLOR_INPUT_OCHRE   = new Color(241, 201, 122);      // Pincelada input
    private final Color COLOR_RED_PIN       = new Color(200, 50, 40);        // Alfinete
    private final Color COLOR_CORAL         = new Color(224, 122, 95);       // Coral/XP delete
    private final Color COLOR_PURPLE_BADGE  = new Color(157, 116, 194);      // Badge XP
    private final Color COLOR_PILL_LEITURA  = new Color(82, 135, 111);       // Pill leitura
    private final Color COLOR_PILL_ARTE     = new Color(241, 160, 196);      // Pill arte
    private final Color COLOR_PILL_FITNESS  = new Color(168, 214, 72);       // Pill fitness
    private final Color COLOR_NAV_BG        = new Color(229, 217, 197);      // Fundo nav
    private final Color COLOR_BLUE_LINK     = new Color(91, 155, 213);       // Link esqueci senha
    private final Color COLOR_CARD_YELLOW   = new Color(253, 242, 195);      // Card hobby yellow
    private final Color COLOR_AJUDA_BG      = new Color(254, 248, 218);      // Fundo amarelado da ajuda

    // Campos de Entrada do Formulário de Login / Cadastro
    private JTextField txtEmail;
    private JPasswordField txtSenha;
    private JPasswordField txtConfirmaSenha;
    private JTextField txtNomeCadastro;

    // Botões de olho para mostrar/ocultar senha
    private boolean senhaVisivel = false;
    private boolean confirmaSenhaVisivel = false;
    private Rectangle btnOlhoSenha = new Rectangle();
    private Rectangle btnOlhoConfirmaSenha = new Rectangle();

    // Dados do Jogador
    private String nomeJogador = "Aventureiro";
    private String emailJogador = "aluno@escola.com";
    private int streakDias = 1;
    private int totalXP = 25;
    private int totalDesafios = 0;
    private int xpLeitura = 15, nivelLeitura = 1;
    private int xpArte = 10, nivelArte = 1;
    private int xpFitness = 0, nivelFitness = 1;

    // Classe para representar uma meta individual
    public static class MetaItem {
        public String texto;
        public int xp;
        public boolean concluida;
        public Rectangle checkRect = new Rectangle();
        public Rectangle deleteRect = new Rectangle();

        public MetaItem(String texto, int xp, boolean concluida) {
            this.texto = texto;
            this.xp = xp;
            this.concluida = concluida;
        }
    }

    // Objetivos dinâmicos
    private List<MetaItem> metasLeitura = new ArrayList<>();
    private List<MetaItem> metasArte = new ArrayList<>();
    private List<MetaItem> metasFitness = new ArrayList<>();

    // Campos de texto para adicionar novas metas
    private JTextField txtNovaMetaLeitura;
    private JTextField txtNovaMetaArte;
    private JTextField txtNovaMetaFitness;

    // Áreas clicáveis dos botões + e ↺
    private Rectangle btnAddLeitura = new Rectangle();
    private Rectangle btnRefLeitura = new Rectangle();
    private Rectangle btnAddArte = new Rectangle();
    private Rectangle btnRefArte = new Rectangle();
    private Rectangle btnAddFitness = new Rectangle();
    private Rectangle btnRefFitness = new Rectangle();

    // Histórico de ações para o botão Desfazer / Apagar o que fiz anteriormente (↺)
    private enum TipoAcaoObjetivo {
        ADICIONOU,
        TOGGLE_CHECK,
        REMOVEU
    }

    private static class HistoricoAcao {
        TipoAcaoObjetivo tipo;
        MetaItem item;
        int index;

        HistoricoAcao(TipoAcaoObjetivo tipo, MetaItem item, int index) {
            this.tipo = tipo;
            this.item = item;
            this.index = index;
        }
    }

    private List<HistoricoAcao> historicoLeitura = new ArrayList<>();
    private List<HistoricoAcao> historicoArte = new ArrayList<>();
    private List<HistoricoAcao> historicoFitness = new ArrayList<>();

    // Animação de popup de XP quando uma tarefa é concluída (+XP flutuante que aparece e desaparece)
    public static class AnimacaoXP {
        public String texto;
        public int x;
        public int y;
        public long inicio;
        public final int duracao = 1500; // 1.5s

        public AnimacaoXP(String texto, int x, int y) {
            this.texto = texto;
            this.x = x;
            this.y = y;
            this.inicio = System.currentTimeMillis();
        }

        public boolean estaAtiva() {
            return (System.currentTimeMillis() - inicio) < duracao;
        }

        public float getProgresso() {
            return Math.min(1.0f, (float)(System.currentTimeMillis() - inicio) / duracao);
        }
    }

    private List<AnimacaoXP> animacoesXP = new ArrayList<>();

    // Avatares
    private String[] avatarNomes = {"Gabi", "Victor", "Nicollas", "Quel", "Camiz", "May", "Ivi"};
    private String[] avatarArquivos = {
        "gabi_personagem-removebg-preview.png",
        "victor_portrait.png",
        "portrait_nicollas-removebg-preview.png",
        "quel (1).png",
        "camiz (1).png",
        "may (1).png",
        "ivi_portrait.png"
    };
    private BufferedImage[] avatarImagens = new BufferedImage[7];
    private Rectangle[] avatarLimites = new Rectangle[7];
    private int avatarSelecionado = 0;
    private BufferedImage imgFundoCaderno = null;
    private BufferedImage imgBtnEntrar = null;
    private BufferedImage imgTituloHobbyQuest = null;
    private BufferedImage imgFaixaEsqueciSenha = null;
    private BufferedImage imgPostItAlfinete = null;
    private BufferedImage imgFaixaLogin = null;
    private BufferedImage imgInputVerde = null;
    private BufferedImage imgAdesivoScrapbook = null;

    // Áreas Clicáveis
    private Rectangle btnPostItToggle = new Rectangle();
    private Rectangle btnSubmitAuth = new Rectangle();
    private Rectangle btnEsqueciSenha = new Rectangle();
    private static final class LayoutLogin {
        int centerX, inputX, inputW, inputH, inputGap;
        int titleX, titleY, titleW, titleH;
        int stripX, stripY, stripW, stripH, baseY;
        int submitX, submitY, submitW, submitH;
        int forgotY, sideLeftX, sideRightX, sideY, sideSize;

        int inputY(int row) {
            return baseY + row * (inputH + inputGap);
        }
    }

    private Rectangle btnIniciarJornada = new Rectangle();
    private Rectangle cardArte = new Rectangle();
    private Rectangle cardLeitura = new Rectangle();
    private Rectangle cardFitness = new Rectangle();
    private Rectangle[] navTabs = new Rectangle[5];
    private String[] navNomes = {"HOME", "OBJETIVOS", "DESEMPENHO", "AJUDA", "PERFIL"};
    private Rectangle btnVoltarSubTela = new Rectangle();
    private Rectangle btnAbrirConquistas = new Rectangle();
    private Rectangle btnTrocarNome = new Rectangle();
    private Rectangle btnLogout = new Rectangle();
    private Rectangle[] avatarBotoes = new Rectangle[7];
    private Rectangle btnUploadFoto = new Rectangle();
    private BufferedImage fotoCustomizada = null;
    private Rectangle avatarLimiteCustom = null;
    // Overlay de conquistas
    private boolean mostrandoConquistas = false;
    private Rectangle btnFecharConquistas = new Rectangle();

    // Hover
    private boolean hoverIniciar = false;
    private int hoverCard = -1;
    private int hoverTab = -1;
    private boolean hoverSubmit = false;
    private boolean hoverPostIt = false;
    private boolean hoverEsqueci = false;
    private boolean hoverOlho = false;

    // Valores interpolados para animacao suave de hover (0.0f a 1.0f)
    private float hoverSubmitAnim = 0.0f;
    private float hoverPostItAnim = 0.0f;
    private float hoverEsqueciAnim = 0.0f;
    private float hoverOlhoAnim = 0.0f;
    private float hoverIniciarAnim = 0.0f;

    // Scroll da tela
    private int scrollY = 0;
    private int maxScrollObjetivos = 0;

    public HobbyQuestAppPanel(JogoAudrey frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setBackground(COLOR_BG_PAGE);
        setLayout(null);
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(e -> {
            if (telaAtual == 1 && maxScrollObjetivos > 0) {
                scrollY = Math.max(0, Math.min(maxScrollObjetivos, scrollY + e.getWheelRotation() * 30));
                atualizarVisibilidadeCampos();
                repaint();
            }
        });

        for (int i = 0; i < 5; i++) navTabs[i] = new Rectangle();
        for (int i = 0; i < 7; i++) avatarBotoes[i] = new Rectangle();

        carregarFontes();
        carregarFundo();
        carregarImagensElementos();
        carregarAvatares();
        inicializarCamposLogin();
        inicializarCamposObjetivos();

        animTimer = new Timer(30, this);
        animTimer.start();

        ToolTipManager.sharedInstance().registerComponent(this);
    }

    private void carregarFontes() {
        try {
            fontCabin = Font.createFont(Font.TRUETYPE_FONT, new File(JogoAudrey.resolvePath("CrayonHandRegular2016Demo.ttf"))).deriveFont(22f);
            fontTitulo = Font.createFont(Font.TRUETYPE_FONT, new File(JogoAudrey.resolvePath("KGSecondChancesSketch.ttf"))).deriveFont(48f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontCabin);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontTitulo);
        } catch (Exception e) {
            fontCabin = new Font("Comic Sans MS", Font.BOLD, 20);
            fontTitulo = new Font("Comic Sans MS", Font.BOLD, 44);
        }
        fontCaveat = new Font("SansSerif", Font.ITALIC, 18);
        fontWalter = new Font("SansSerif", Font.BOLD, 14);
    }

    private void carregarFundo() {
        try {
            String path = JogoAudrey.resolvePath("fundo_caderno.png");
            if (path != null) {
                File f = new File(path);
                if (f.exists()) {
                    imgFundoCaderno = ImageIO.read(f);
                }
            }
        } catch (Exception ignored) {}
    }

    private void carregarImagensElementos() {
        try {
            String p1 = JogoAudrey.resolvePath("btn_entrar_costurado.png");
            if (p1 != null) { File f = new File(p1); if (f.exists()) imgBtnEntrar = ImageIO.read(f); }
        } catch (Exception ignored) {}
        try {
            String p2 = JogoAudrey.resolvePath("titulo_hobbyquest_recorte_transp.png");
            if (p2 == null || !new File(p2).exists()) p2 = JogoAudrey.resolvePath("titulo_hobbyquest_recorte.png");
            if (p2 != null) { File f = new File(p2); if (f.exists()) imgTituloHobbyQuest = ImageIO.read(f); }
        } catch (Exception ignored) {}
        try {
            String p3 = JogoAudrey.resolvePath("faixa_esqueci_senha.png");
            if (p3 != null) { File f = new File(p3); if (f.exists()) imgFaixaEsqueciSenha = ImageIO.read(f); }
        } catch (Exception ignored) {}
        try {
            String p4 = JogoAudrey.resolvePath("postit_alfinete_vermelho.png");
            if (p4 != null) { File f = new File(p4); if (f.exists()) imgPostItAlfinete = ImageIO.read(f); }
        } catch (Exception ignored) {}
        try {
            String p5 = JogoAudrey.resolvePath("faixa_login_recorte.png");
            if (p5 != null) { File f = new File(p5); if (f.exists()) imgFaixaLogin = ImageIO.read(f); }
        } catch (Exception ignored) {}
        try {
            String p6 = JogoAudrey.resolvePath("input_pilula_verde.png");
            if (p6 != null) { File f = new File(p6); if (f.exists()) imgInputVerde = ImageIO.read(f); }
        } catch (Exception ignored) {}
        try {
            String p7 = JogoAudrey.resolvePath("adesivo_scrapbook.png");
            if (p7 != null) { File f = new File(p7); if (f.exists()) imgAdesivoScrapbook = ImageIO.read(f); }
        } catch (Exception ignored) {}
    }

    private void carregarAvatares() {
        for (int i = 0; i < avatarArquivos.length; i++) {
            try {
                String p = JogoAudrey.resolvePath(avatarArquivos[i]);
                if (p != null) {
                    File f = new File(p);
                    if (f.exists()) {
                        avatarImagens[i] = ImageIO.read(f);
                        avatarLimites[i] = calcularLimitesConteudo(avatarImagens[i]);
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private Rectangle calcularLimitesConteudo(BufferedImage img) {
        if (img == null) return null;
        int w = img.getWidth();
        int h = img.getHeight();
        int minX = w, maxX = 0, minY = h, maxY = 0;
        boolean found = false;
        for (int y = 0; y < h; y += 2) {
            for (int x = 0; x < w; x += 2) {
                int rgb = img.getRGB(x, y);
                int a = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (a > 25 && (r < 250 || g < 250 || b < 250)) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                    found = true;
                }
            }
        }
        if (!found) return new Rectangle(0, 0, w, h);
        return new Rectangle(minX, minY, Math.max(1, maxX - minX), Math.max(1, maxY - minY));
    }

    private void desenharAvatarProporcional(Graphics2D g2, int index, int destX, int destY, int destW, int destH, boolean isCircle) {
        if (index < 0 || index >= avatarImagens.length || avatarImagens[index] == null) return;
        BufferedImage img = avatarImagens[index];
        Rectangle b = (avatarLimites != null && index < avatarLimites.length) ? avatarLimites[index] : null;
        if (b == null) {
            b = new Rectangle(0, 0, img.getWidth(), img.getHeight());
        }

        // Foco no rosto/cabeça: enquadramento proporcional perfeitamente quadrado (1:1)
        double boxSize = Math.max(b.width, b.height * 0.88);
        boxSize = Math.min(boxSize, Math.min(img.getWidth(), img.getHeight()));

        double srcX = b.x + (b.width - boxSize) / 2.0;
        double srcY = b.y - (boxSize * 0.04);

        if (srcX < 0) srcX = 0;
        if (srcY < 0) srcY = 0;
        if (srcX + boxSize > img.getWidth()) srcX = Math.max(0, img.getWidth() - boxSize);
        if (srcY + boxSize > img.getHeight()) srcY = Math.max(0, img.getHeight() - boxSize);

        // Garantir que a área de destino seja perfeitamente quadrada para manter proporção 1:1 exata
        int finalDestX = destX;
        int finalDestY = destY;
        int finalDestW = destW;
        int finalDestH = destH;
        if (destW != destH) {
            int minDim = Math.min(destW, destH);
            finalDestX = destX + (destW - minDim) / 2;
            finalDestY = destY + (destH - minDim) / 2;
            finalDestW = minDim;
            finalDestH = minDim;
        }

        Shape oldClip = g2.getClip();
        Object oldHint = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (isCircle) {
            g2.setClip(new Ellipse2D.Float(finalDestX, finalDestY, finalDestW, finalDestH));
        } else {
            g2.setClip(new Rectangle(finalDestX, finalDestY, finalDestW, finalDestH));
        }

        g2.drawImage(img,
            finalDestX, finalDestY, finalDestX + finalDestW, finalDestY + finalDestH,
            (int)srcX, (int)srcY, (int)(srcX + boxSize), (int)(srcY + boxSize),
            null
        );

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldHint != null ? oldHint : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setClip(oldClip);
    }

    private void inicializarCamposLogin() {
        Font fontInput = fontCabin.deriveFont(20f);

        txtNomeCadastro = criarCampoTexto(fontInput);
        txtEmail = criarCampoTexto(fontInput);
        txtSenha = criarCampoSenha(fontInput);
        txtConfirmaSenha = criarCampoSenha(fontInput);

        add(txtNomeCadastro);
        add(txtEmail);
        add(txtSenha);
        add(txtConfirmaSenha);

        atualizarVisibilidadeCampos();
    }

    private JTextField criarCampoTexto(Font f) {
        JTextField tf = new JTextField();
        tf.setFont(f);
        Color corInputTexto = new Color(30, 60, 25);
        tf.setForeground(corInputTexto);
        tf.setCaretColor(corInputTexto);
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setOpaque(false);
        tf.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return tf;
    }

    private JPasswordField criarCampoSenha(Font f) {
        JPasswordField pf = new JPasswordField();
        pf.setFont(f);
        Color corInputTexto = new Color(30, 60, 25);
        pf.setForeground(corInputTexto);
        pf.setCaretColor(corInputTexto);
        pf.setHorizontalAlignment(JTextField.CENTER);
        pf.setOpaque(false);
        pf.setBorder(BorderFactory.createEmptyBorder(4, 30, 4, 30));
        pf.setEchoChar('\u2022'); // Bullet character •
        return pf;
    }

    private void inicializarCamposObjetivos() {
        if (metasLeitura.isEmpty()) {
            metasLeitura.add(new MetaItem("Ler livro do armário", 60, false));
        }
        if (metasArte.isEmpty()) {
            metasArte.add(new MetaItem("Recriar com seus traços uma\nobra de arte famosa", 100, false));
        }
        if (metasFitness.isEmpty()) {
            metasFitness.add(new MetaItem("15 minutos de caminhada ou corrida", 80, false));
        }

        txtNovaMetaLeitura = criarCampoMeta("Nova meta de leitura...");
        txtNovaMetaArte = criarCampoMeta("Nova meta de arte...");
        txtNovaMetaFitness = criarCampoMeta("Nova meta de fitness...");

        txtNovaMetaLeitura.addActionListener(e -> adicionarMetaLeitura());
        txtNovaMetaArte.addActionListener(e -> adicionarMetaArte());
        txtNovaMetaFitness.addActionListener(e -> adicionarMetaFitness());

        add(txtNovaMetaLeitura);
        add(txtNovaMetaArte);
        add(txtNovaMetaFitness);
    }

    private JTextField criarCampoMeta(String placeholder) {
        JTextField tf = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setFont(fontCaveat != null ? fontCaveat.deriveFont(Font.ITALIC, 15f) : new Font("SansSerif", Font.ITALIC, 15));
                    g2.setColor(new Color(160, 160, 160));
                    g2.drawString(placeholder, 8, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        tf.setFont(fontCaveat != null ? fontCaveat.deriveFont(Font.PLAIN, 16f) : new Font("SansSerif", Font.PLAIN, 16));
        tf.setForeground(new Color(40, 40, 40));
        tf.setCaretColor(COLOR_DARK_GREEN);
        tf.setOpaque(false);
        tf.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        tf.setVisible(false);
        return tf;
    }

    private void adicionarMetaLeitura() {
        if (txtNovaMetaLeitura == null) return;
        String texto = txtNovaMetaLeitura.getText().trim();
        if (!texto.isEmpty()) {
            if (metasLeitura.size() >= 4) {
                JOptionPane.showMessageDialog(this, "Você já possui 4 metas de leitura ativas! Conclua ou remova uma antes.", "Hobby Quest", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            MetaItem item = new MetaItem(texto, 50, false);
            metasLeitura.add(item);
            historicoLeitura.add(new HistoricoAcao(TipoAcaoObjetivo.ADICIONOU, item, metasLeitura.size() - 1));
            txtNovaMetaLeitura.setText("");
            repaint();
        }
    }

    private void adicionarMetaArte() {
        if (txtNovaMetaArte == null) return;
        String texto = txtNovaMetaArte.getText().trim();
        if (!texto.isEmpty()) {
            if (metasArte.size() >= 5) {
                JOptionPane.showMessageDialog(this, "Você já possui 5 metas de arte ativas! Conclua ou remova uma antes.", "Hobby Quest", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            MetaItem item = new MetaItem(texto, 50, false);
            metasArte.add(item);
            historicoArte.add(new HistoricoAcao(TipoAcaoObjetivo.ADICIONOU, item, metasArte.size() - 1));
            txtNovaMetaArte.setText("");
            repaint();
        }
    }

    private void adicionarMetaFitness() {
        if (txtNovaMetaFitness == null) return;
        String texto = txtNovaMetaFitness.getText().trim();
        if (!texto.isEmpty()) {
            if (metasFitness.size() >= 5) {
                JOptionPane.showMessageDialog(this, "Você já possui 5 metas de fitness ativas! Conclua ou remova uma antes.", "Hobby Quest", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            MetaItem item = new MetaItem(texto, 60, false);
            metasFitness.add(item);
            historicoFitness.add(new HistoricoAcao(TipoAcaoObjetivo.ADICIONOU, item, metasFitness.size() - 1));
            txtNovaMetaFitness.setText("");
            repaint();
        }
    }

    // Botão ↺: Apaga o que fez anteriormente (limpa texto digitado ou desfaz a última ação)
    private void desfazerLeitura() {
        // 1. Se tem texto digitado no campo de texto, limpa o texto
        if (txtNovaMetaLeitura != null && !txtNovaMetaLeitura.getText().trim().isEmpty()) {
            txtNovaMetaLeitura.setText("");
            repaint();
            return;
        }

        // 2. Se há ações registradas no histórico, desfaz a última
        if (!historicoLeitura.isEmpty()) {
            HistoricoAcao acao = historicoLeitura.remove(historicoLeitura.size() - 1);
            if (acao.tipo == TipoAcaoObjetivo.ADICIONOU) {
                if (acao.item.concluida) {
                    totalXP = Math.max(0, totalXP - acao.item.xp);
                    xpLeitura = Math.max(0, xpLeitura - acao.item.xp);
                    totalDesafios = Math.max(0, totalDesafios - 1);
                }
                metasLeitura.remove(acao.item);
            } else if (acao.tipo == TipoAcaoObjetivo.TOGGLE_CHECK) {
                acao.item.concluida = !acao.item.concluida;
                if (acao.item.concluida) {
                    totalXP += acao.item.xp;
                    xpLeitura += acao.item.xp;
                    totalDesafios++;
                } else {
                    totalXP = Math.max(0, totalXP - acao.item.xp);
                    xpLeitura = Math.max(0, xpLeitura - acao.item.xp);
                    totalDesafios = Math.max(0, totalDesafios - 1);
                }
            } else if (acao.tipo == TipoAcaoObjetivo.REMOVEU) {
                int pos = Math.min(acao.index, metasLeitura.size());
                metasLeitura.add(pos, acao.item);
                if (acao.item.concluida) {
                    totalXP += acao.item.xp;
                    xpLeitura += acao.item.xp;
                    totalDesafios++;
                }
            }
            repaint();
            return;
        }

        // 3. Fallback: desmarca a última meta concluída ou apaga a última adicionada
        for (int i = metasLeitura.size() - 1; i >= 0; i--) {
            MetaItem item = metasLeitura.get(i);
            if (item.concluida) {
                item.concluida = false;
                totalXP = Math.max(0, totalXP - item.xp);
                xpLeitura = Math.max(0, xpLeitura - item.xp);
                totalDesafios = Math.max(0, totalDesafios - 1);
                repaint();
                return;
            }
        }
        if (metasLeitura.size() > 1) {
            MetaItem rem = metasLeitura.remove(metasLeitura.size() - 1);
            if (rem.concluida) {
                totalXP = Math.max(0, totalXP - rem.xp);
                xpLeitura = Math.max(0, xpLeitura - rem.xp);
                totalDesafios = Math.max(0, totalDesafios - 1);
            }
            repaint();
        }
    }

    private void desfazerArte() {
        // 1. Se tem texto digitado no campo de texto, limpa o texto
        if (txtNovaMetaArte != null && !txtNovaMetaArte.getText().trim().isEmpty()) {
            txtNovaMetaArte.setText("");
            repaint();
            return;
        }

        // 2. Se há ações registradas no histórico, desfaz a última
        if (!historicoArte.isEmpty()) {
            HistoricoAcao acao = historicoArte.remove(historicoArte.size() - 1);
            if (acao.tipo == TipoAcaoObjetivo.ADICIONOU) {
                if (acao.item.concluida) {
                    totalXP = Math.max(0, totalXP - acao.item.xp);
                    xpArte = Math.max(0, xpArte - acao.item.xp);
                    totalDesafios = Math.max(0, totalDesafios - 1);
                }
                metasArte.remove(acao.item);
            } else if (acao.tipo == TipoAcaoObjetivo.TOGGLE_CHECK) {
                acao.item.concluida = !acao.item.concluida;
                if (acao.item.concluida) {
                    totalXP += acao.item.xp;
                    xpArte += acao.item.xp;
                    totalDesafios++;
                } else {
                    totalXP = Math.max(0, totalXP - acao.item.xp);
                    xpArte = Math.max(0, xpArte - acao.item.xp);
                    totalDesafios = Math.max(0, totalDesafios - 1);
                }
            } else if (acao.tipo == TipoAcaoObjetivo.REMOVEU) {
                int pos = Math.min(acao.index, metasArte.size());
                metasArte.add(pos, acao.item);
                if (acao.item.concluida) {
                    totalXP += acao.item.xp;
                    xpArte += acao.item.xp;
                    totalDesafios++;
                }
            }
            repaint();
            return;
        }

        // 3. Fallback: desmarca a última meta concluída ou apaga a última adicionada
        for (int i = metasArte.size() - 1; i >= 0; i--) {
            MetaItem item = metasArte.get(i);
            if (item.concluida) {
                item.concluida = false;
                totalXP = Math.max(0, totalXP - item.xp);
                xpArte = Math.max(0, xpArte - item.xp);
                totalDesafios = Math.max(0, totalDesafios - 1);
                repaint();
                return;
            }
        }
        if (metasArte.size() > 1) {
            MetaItem rem = metasArte.remove(metasArte.size() - 1);
            if (rem.concluida) {
                totalXP = Math.max(0, totalXP - rem.xp);
                xpArte = Math.max(0, xpArte - rem.xp);
                totalDesafios = Math.max(0, totalDesafios - 1);
            }
            repaint();
        }
    }

    private void desfazerFitness() {
        // 1. Se tem texto digitado no campo de texto, limpa o texto
        if (txtNovaMetaFitness != null && !txtNovaMetaFitness.getText().trim().isEmpty()) {
            txtNovaMetaFitness.setText("");
            repaint();
            return;
        }

        // 2. Se há ações registradas no histórico, desfaz a última
        if (!historicoFitness.isEmpty()) {
            HistoricoAcao acao = historicoFitness.remove(historicoFitness.size() - 1);
            if (acao.tipo == TipoAcaoObjetivo.ADICIONOU) {
                if (acao.item.concluida) {
                    totalXP = Math.max(0, totalXP - acao.item.xp);
                    xpFitness = Math.max(0, xpFitness - acao.item.xp);
                    totalDesafios = Math.max(0, totalDesafios - 1);
                }
                metasFitness.remove(acao.item);
            } else if (acao.tipo == TipoAcaoObjetivo.TOGGLE_CHECK) {
                acao.item.concluida = !acao.item.concluida;
                if (acao.item.concluida) {
                    totalXP += acao.item.xp;
                    xpFitness += acao.item.xp;
                    totalDesafios++;
                } else {
                    totalXP = Math.max(0, totalXP - acao.item.xp);
                    xpFitness = Math.max(0, xpFitness - acao.item.xp);
                    totalDesafios = Math.max(0, totalDesafios - 1);
                }
            } else if (acao.tipo == TipoAcaoObjetivo.REMOVEU) {
                int pos = Math.min(acao.index, metasFitness.size());
                metasFitness.add(pos, acao.item);
                if (acao.item.concluida) {
                    totalXP += acao.item.xp;
                    xpFitness += acao.item.xp;
                    totalDesafios++;
                }
            }
            repaint();
            return;
        }

        // 3. Fallback: desmarca a última meta concluída ou apaga a última adicionada
        for (int i = metasFitness.size() - 1; i >= 0; i--) {
            MetaItem item = metasFitness.get(i);
            if (item.concluida) {
                item.concluida = false;
                totalXP = Math.max(0, totalXP - item.xp);
                xpFitness = Math.max(0, xpFitness - item.xp);
                totalDesafios = Math.max(0, totalDesafios - 1);
                repaint();
                return;
            }
        }
        if (metasFitness.size() > 1) {
            MetaItem rem = metasFitness.remove(metasFitness.size() - 1);
            if (rem.concluida) {
                totalXP = Math.max(0, totalXP - rem.xp);
                xpFitness = Math.max(0, xpFitness - rem.xp);
                totalDesafios = Math.max(0, totalDesafios - 1);
            }
            repaint();
        }
    }

    private LayoutLogin calcularLayoutLogin(int w, int h) {
        LayoutLogin layout = new LayoutLogin();
        layout.centerX = w / 2;

        // Dimensões do título HOBBY QUEST
        double titleAspect = imgTituloHobbyQuest != null && imgTituloHobbyQuest.getHeight() > 0
                ? (double)imgTituloHobbyQuest.getWidth() / imgTituloHobbyQuest.getHeight()
                : 2.66;
        layout.titleH = Math.min(125, Math.max(85, (int)(h * 0.155f)));
        layout.titleW = (int) Math.round(layout.titleH * titleAspect);
        if (layout.titleW > (int)(w * 0.48f)) {
            layout.titleW = (int)(w * 0.48f);
            layout.titleH = (int) Math.round(layout.titleW / titleAspect);
        }
        layout.titleX = layout.centerX - layout.titleW / 2;

        // Dimensões da faixa LOGIN
        layout.stripW = Math.max(180, Math.min(270, (int)(w * 0.28f)));
        layout.stripH = Math.min(58, Math.max(46, (int)(h * 0.075f)));
        layout.stripX = layout.centerX - layout.stripW / 2;

        // Dimensões dos campos de input (pílula verde)
        layout.inputW = Math.max(270, Math.min(340, (int)(w * 0.35f)));
        layout.inputH = Math.min(50, Math.max(42, (int)(h * 0.065f)));
        layout.inputX = layout.centerX - layout.inputW / 2;

        // Espaçamento bem respirado entre os inputs
        layout.inputGap = Math.min(24, Math.max(18, (int)(h * 0.026f)));

        // Dimensões do botão ENTRAR
        layout.submitW = Math.min(230, Math.max(190, (int)(layout.inputW * 0.72f)));
        layout.submitH = Math.min(62, Math.max(48, (int)(h * 0.078f)));
        layout.submitX = layout.centerX - layout.submitW / 2;

        // GAPs verticais bem separados para não ficarem grudados
        int gapTitleToStrip = Math.min(34, Math.max(22, (int)(h * 0.035f)));
        int gapStripToInput = Math.min(34, Math.max(22, (int)(h * 0.035f)));
        int gapInputToForgot = Math.min(26, Math.max(18, (int)(h * 0.026f)));
        int gapForgotToSubmit = Math.min(28, Math.max(20, (int)(h * 0.028f)));
        int gapInputToSubmitCadastro = Math.min(30, Math.max(22, (int)(h * 0.030f)));

        int inputCount = modoCadastro ? 4 : 2;
        int inputsH = inputCount * layout.inputH + (inputCount - 1) * layout.inputGap;

        int totalFormH;
        if (modoCadastro) {
            totalFormH = layout.titleH + gapTitleToStrip + layout.stripH + gapStripToInput + inputsH + gapInputToSubmitCadastro + layout.submitH;
        } else {
            totalFormH = layout.titleH + gapTitleToStrip + layout.stripH + gapStripToInput + inputsH + gapInputToForgot + 34 + gapForgotToSubmit + layout.submitH;
        }

        // Posicionamento vertical centralizado e harmonioso
        int startY = Math.max(22, (h - totalFormH) / 2 - 12);
        layout.titleY = startY;
        layout.stripY = layout.titleY + layout.titleH + gapTitleToStrip;
        layout.baseY = layout.stripY + layout.stripH + gapStripToInput;

        if (modoCadastro) {
            layout.submitY = layout.inputY(inputCount - 1) + layout.inputH + gapInputToSubmitCadastro;
        } else {
            int lastInputBottom = layout.inputY(inputCount - 1) + layout.inputH;
            int forgotTop = lastInputBottom + gapInputToForgot;
            layout.forgotY = forgotTop + 17; // centro vertical da etiqueta (34px / 2)
            layout.submitY = (forgotTop + 34) + gapForgotToSubmit;
        }

        // Laterais (Sticker à esquerda e Post-it à direita) bem afastados dos inputs
        int sideGap = Math.min(80, Math.max(45, (int)(w * 0.05f)));
        int maxSideSizeW = Math.max(0, (w - layout.inputW) / 2 - sideGap - 20);
        layout.sideSize = Math.min(160, Math.max(110, Math.min(maxSideSizeW, (int)(h * 0.24f))));
        layout.sideLeftX = layout.inputX - sideGap - layout.sideSize;
        layout.sideRightX = layout.inputX + layout.inputW + sideGap;

        // Alinhamento vertical do Post-it e Sticker com o miolo dos inputs
        int contentCenterY = (layout.baseY + layout.submitY + layout.submitH) / 2;
        layout.sideY = Math.max(layout.stripY - 10, contentCenterY - layout.sideSize / 2);
        return layout;
    }

    private void atualizarVisibilidadeCampos() {
        int w = getWidth() > 0 ? getWidth() : LARGURA;
        int h = getHeight() > 0 ? getHeight() : ALTURA;

        if (estaLogado) {
            txtNomeCadastro.setVisible(false);
            txtEmail.setVisible(false);
            txtSenha.setVisible(false);
            txtConfirmaSenha.setVisible(false);

            boolean naTelaObjetivos = (telaAtual == 1);
            if (txtNovaMetaLeitura != null) txtNovaMetaLeitura.setVisible(naTelaObjetivos);
            if (txtNovaMetaArte != null) txtNovaMetaArte.setVisible(naTelaObjetivos);
            if (txtNovaMetaFitness != null) txtNovaMetaFitness.setVisible(naTelaObjetivos);
        } else {
            if (txtNovaMetaLeitura != null) txtNovaMetaLeitura.setVisible(false);
            if (txtNovaMetaArte != null) txtNovaMetaArte.setVisible(false);
            if (txtNovaMetaFitness != null) txtNovaMetaFitness.setVisible(false);

            LayoutLogin layout = calcularLayoutLogin(w, h);
            int textInset = Math.min(16, layout.inputW / 10);
            int eyeWidth = Math.min(38, layout.inputW / 5);

            if (modoCadastro) {
                txtNomeCadastro.setVisible(true);
                txtEmail.setVisible(true);
                txtSenha.setVisible(true);
                txtConfirmaSenha.setVisible(true);

                txtNomeCadastro.setBounds(layout.inputX + textInset, layout.inputY(0) + 5,
                        Math.max(0, layout.inputW - textInset * 2), layout.inputH - 10);
                txtEmail.setBounds(layout.inputX + textInset, layout.inputY(1) + 5,
                        Math.max(0, layout.inputW - textInset * 2), layout.inputH - 10);
                txtSenha.setBounds(layout.inputX + textInset, layout.inputY(2) + 5,
                        Math.max(0, layout.inputW - textInset * 2 - eyeWidth), layout.inputH - 10);
                txtConfirmaSenha.setBounds(layout.inputX + textInset, layout.inputY(3) + 5,
                        Math.max(0, layout.inputW - textInset * 2 - eyeWidth), layout.inputH - 10);
            } else {
                txtNomeCadastro.setVisible(false);
                txtConfirmaSenha.setVisible(false);
                txtEmail.setVisible(true);
                txtSenha.setVisible(true);

                txtEmail.setBounds(layout.inputX + textInset, layout.inputY(0) + 5,
                        Math.max(0, layout.inputW - textInset * 2), layout.inputH - 10);
                txtSenha.setBounds(layout.inputX + textInset, layout.inputY(1) + 5,
                        Math.max(0, layout.inputW - textInset * 2 - eyeWidth), layout.inputH - 10);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        animTicks += 0.05f;

        // Atualizar animação de transição
        if (transTelaDestino >= 0) {
            long agora = System.currentTimeMillis();
            float progresso = Math.min(1.0f, (float)(agora - transInicio) / TRANS_DURACAO);
            // Ease-out cubic
            float ease = 1.0f - (1.0f - progresso) * (1.0f - progresso) * (1.0f - progresso);

            if (transindoSaida) {
                transAlpha = 1.0f - ease;
                transSlideX = ease * 40; // desliza 40px para direita
                if (progresso >= 1.0f) {
                    // Fase de saída completa — troca a tela e inicia entrada
                    telaAtual = transTelaDestino;
                    scrollY = 0;
                    atualizarVisibilidadeCampos();
                    transindoSaida = false;
                    transInicio = agora;
                    transAlpha = 0f;
                    transSlideX = -40; // entra da esquerda
                }
            } else {
                transAlpha = ease;
                transSlideX = -40 * (1.0f - ease);
                if (progresso >= 1.0f) {
                    // Transição completa
                    transTelaDestino = -1;
                    transAlpha = 1.0f;
                    transSlideX = 0f;
                }
            }
        }

        // Atualizar animação de clique
        // Atualizar suavizacao das animacoes de hover (transicao fluida)
        float lerpVel = 0.28f;
        hoverSubmitAnim += ((hoverSubmit ? 1.0f : 0.0f) - hoverSubmitAnim) * lerpVel;
        hoverPostItAnim += ((hoverPostIt ? 1.0f : 0.0f) - hoverPostItAnim) * lerpVel;
        hoverEsqueciAnim += ((hoverEsqueci ? 1.0f : 0.0f) - hoverEsqueciAnim) * lerpVel;
        hoverOlhoAnim += ((hoverOlho ? 1.0f : 0.0f) - hoverOlhoAnim) * lerpVel;
        hoverIniciarAnim += ((hoverIniciar ? 1.0f : 0.0f) - hoverIniciarAnim) * lerpVel;

        if (clickRect != null) {
            long agora = System.currentTimeMillis();
            float progresso = Math.min(1.0f, (float)(agora - clickInicio) / CLICK_DURACAO);
            if (progresso < 0.5f) {
                clickScale = 1.0f - 0.08f * (progresso * 2); // shrink
            } else {
                clickScale = 0.92f + 0.08f * ((progresso - 0.5f) * 2); // bounce back
            }
            if (progresso >= 1.0f) {
                clickRect = null;
                clickScale = 1.0f;
            }
        }

        repaint();
    }

    /** Inicia uma transição animada para outra tela */
    private void iniciarTransicao(int destino) {
        if (transTelaDestino >= 0) return; // já em transição
        if (destino == telaAtual) return;
        transTelaDestino = destino;
        transindoSaida = true;
        transInicio = System.currentTimeMillis();
    }

    /** Inicia animação de clique em um retângulo */
    private void iniciarAnimacaoClique(Rectangle rect) {
        clickRect = rect;
        clickInicio = System.currentTimeMillis();
        clickScale = 1.0f;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();
        CONTAINER_W = getContainerW();
        int cx = (w - CONTAINER_W) / 2;
        sincronizarNiveis();

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        // Fundo do Caderno
        desenharFundoCaderno(g2, w, h);

        // Se houver margem lateral (tela cheia ou monitor largo), preencher com enfeites decorativos (caderno sketch)
        if (estaLogado && cx >= 30) {
            desenharEnfeitesLaterais(g2, cx, w, h);
        }

        // Geometria
        atualizarGeometriaRelativa(cx, w, h);
        atualizarVisibilidadeCampos();

        if (!estaLogado) {
            desenharTelaLogin(g2, cx, w, h);
            // Filtro fofo suave — rosinha/lavanda sobre a tela de login e criar conta
            GradientPaint filtroFofo = new GradientPaint(
                0, 0, new Color(255, 220, 240, 28),   // rosa claro no topo
                w, h, new Color(220, 200, 255, 22)    // lavanda no canto inferior
            );
            Composite oldC = g2.getComposite();
            g2.setPaint(filtroFofo);
            g2.fillRect(0, 0, w, h);
            // Vinheta suave nos cantos
            java.awt.RadialGradientPaint vinheta = new java.awt.RadialGradientPaint(
                w / 2f, h / 2f, Math.max(w, h) * 0.72f,
                new float[]{0.55f, 1.0f},
                new Color[]{new Color(0, 0, 0, 0), new Color(80, 50, 90, 38)}
            );
            g2.setPaint(vinheta);
            g2.fillRect(0, 0, w, h);
            g2.setComposite(oldC);
            return;
        }

        // Efeitos de fundo comuns a todas as telas internas (luz suave + partículas)
        desenharEfeitoLuzESol(g2, w, h);
        desenharDoodlesFundoLogado(g2, cx, w, h);
        desenharParticulasMagicas(g2, w, h);

        // Aplicar transformação de transição ao conteúdo (não à nav)
        AffineTransform oldTransform = g2.getTransform();
        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, transAlpha))));
        g2.translate(transSlideX, 0);

        switch (telaAtual) {
            case 0 -> desenharHome(g2, cx, w, h);
            case 1 -> desenharObjetivos(g2, cx, w, h);
            case 2 -> desenharDesempenho(g2, cx, w, h);
            case 3 -> desenharAjuda(g2, cx, w, h);
            case 4 -> desenharPerfil(g2, cx, w, h);
        }

        // Restaurar transformação para a nav bar (não deve animar)
        g2.setTransform(oldTransform);
        g2.setComposite(oldComposite);

        if (telaAtual <= 4) {
            desenharBarraNavegacao(g2, cx, w, h);
        }

        // Overlay de conquistas (desenhado por cima de tudo)
        if (mostrandoConquistas) {
            desenharOverlayConquistas(g2, w, h);
        }
    }

    /** Mantém os níveis coerentes com o XP acumulado (100 XP por nível). */
    private void sincronizarNiveis() {
        nivelLeitura = 1 + Math.max(0, xpLeitura) / 100;
        nivelArte = 1 + Math.max(0, xpArte) / 100;
        nivelFitness = 1 + Math.max(0, xpFitness) / 100;
    }

    private void atualizarGeometriaRelativa(int cx, int w, int h) {
        CONTAINER_W = getContainerW();
        LayoutLogin layout = calcularLayoutLogin(w, h);

        if (layout.sideSize >= 96) {
            btnPostItToggle.setBounds(layout.sideRightX, layout.sideY, layout.sideSize, layout.sideSize);
        } else {
            btnPostItToggle.setBounds(0, 0, 0, 0);
        }
        btnSubmitAuth.setBounds(layout.submitX, layout.submitY, layout.submitW, layout.submitH);
        btnEsqueciSenha.setBounds(layout.centerX - Math.min(90, layout.inputW / 2),
                layout.forgotY - 17, Math.min(180, layout.inputW), 34);

        int eyeWidth = Math.min(38, layout.inputW / 5);
        int eyeHeight = Math.max(0, layout.inputH - 12);
        int eyeX = layout.inputX + layout.inputW - eyeWidth - 8;
        btnOlhoSenha.setBounds(eyeX, layout.inputY(1) + 6, eyeWidth, eyeHeight);
        btnOlhoConfirmaSenha.setBounds(eyeX, layout.inputY(3) + 6, eyeWidth, eyeHeight);

        // Botão INICIAR JORNADA — proporcional à tela (largura máxima confortável)
        int jornadaW = Math.min(CONTAINER_W - 80, 620);
        btnIniciarJornada.setBounds(cx + (CONTAINER_W - jornadaW) / 2, 146, jornadaW, 72);

        // Cards de hobby — grid centralizada com largura máxima (evita cards gigantes em telas grandes)
        int gridGap = 22;
        int gridMaxW = Math.min(CONTAINER_W - 40, 960);
        int cardW = Math.min((gridMaxW - gridGap * 2) / 3, 300);
        int cardH = Math.max(196, Math.min(272, (int)(cardW * 0.80f) + Math.max(0, (h - 780) / 12)));
        cardH = Math.min(cardH, Math.max(150, (h - 78) - 252 - 110));
        int totalGridW = cardW * 3 + gridGap * 2;
        int gridX = cx + (CONTAINER_W - totalGridW) / 2;
        int gridY = 252;
        cardArte.setBounds(gridX, gridY, cardW, cardH);
        cardLeitura.setBounds(gridX + cardW + gridGap, gridY, cardW, cardH);
        cardFitness.setBounds(gridX + (cardW + gridGap) * 2, gridY, cardW, cardH);

        // Nav bar — proporcional à largura total da janela
        int navTabW = w / 5;
        for (int i = 0; i < 5; i++) {
            navTabs[i].setBounds(i * navTabW, h - 78, navTabW, 78);
        }

        int btnPerfilW = Math.min(CONTAINER_W - 60, 480);
        btnTrocarNome.setBounds(cx + (CONTAINER_W - btnPerfilW) / 2, 285, btnPerfilW, 36);

        int cardRowW = Math.min(CONTAINER_W - 60, 520);
        int cardRowX = cx + (CONTAINER_W - cardRowW) / 2;
        int colItemW = (cardRowW - 20) / 2;
        btnAbrirConquistas.setBounds(cardRowX + colItemW + 20, 450, colItemW, 44);
        btnLogout.setBounds(cx + (CONTAINER_W - btnPerfilW) / 2, 506, btnPerfilW, 38);

        int avatarGap = 10;
        int avatarSize = 50;
        int avatarTotalW = 7 * avatarSize + 6 * avatarGap;
        int avatarStartX = cx + (CONTAINER_W - avatarTotalW) / 2;
        for (int i = 0; i < 7; i++) {
            avatarBotoes[i].setBounds(avatarStartX + i * (avatarSize + avatarGap), 332, avatarSize, avatarSize);
        }

        int uploadW = Math.min(CONTAINER_W - 60, 260);
        btnUploadFoto.setBounds(cx + (CONTAINER_W - uploadW) / 2, 394, uploadW, 34);
    }

    // ==========================================
    // FUNDO DO CADERNO
    // ==========================================
    private void desenharFundoCaderno(Graphics2D g2, int w, int h) {
        // Fundo principal amarelado suave (como caderno antigo)
        g2.setColor(COLOR_BG_PAGE);
        g2.fillRect(0, 0, w, h);

        // Linhas horizontais do caderno (azuis claras e suaves como no design)
        g2.setColor(new Color(210, 222, 235, 180));
        g2.setStroke(new BasicStroke(1.0f));
        for (int y = 45; y < h - 36; y += 32) {
            g2.drawLine(0, y, w, y);
        }

        // Margem vertical do caderno (linha rosa suave tradicional de fichário escolar)
        g2.setColor(new Color(245, 168, 178, 85));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawLine(54, 0, 54, h);
    }

    private void desenharMesaMadeira(Graphics2D g2, int w, int h) {
        int deskH = 38;
        int deskY = h - deskH;

        // Sombra suave do caderno sobre a madeira
        g2.setColor(new Color(0, 0, 0, 45));
        g2.fillRect(0, deskY - 4, w, 4);

        // Madeira com gradiente realista
        GradientPaint wood = new GradientPaint(0, deskY, new Color(138, 92, 58), 0, h, new Color(92, 54, 30));
        g2.setPaint(wood);
        g2.fillRect(0, deskY, w, deskH);

        // Veios e textura da madeira
        g2.setStroke(new BasicStroke(1.0f));
        for (int y = deskY + 6; y < h; y += 6) {
            g2.setColor(new Color(65, 36, 16, 70));
            g2.drawLine(0, y, w, y);
            g2.setColor(new Color(175, 125, 85, 35));
            g2.drawLine(0, y + 2, w, y + 2);
        }
    }

    // ==========================================
    // 1. TELA DE LOGIN
    // ==========================================
    private void desenharTelaLogin(Graphics2D g2, int cx, int w, int h) {
        LayoutLogin layout = calcularLayoutLogin(w, h);

        // 1. Feixes de luz solar suave da manhã
        desenharEfeitoLuzESol(g2, w, h);

        // 2. Doodles do caderno
        desenharDoodlesFundoExatos(g2, w, h);

        // 3. Varalzinho fofo de bandeirolas no topo
        desenharVaralBandeirolas(g2, w, h);

        // ── Sticker oficial da imagem de referência no Canto Superior Direito ──
        // Posicionado bem à direita e para cima, com fita scrapbook
        if (imgAdesivoScrapbook != null) {
            int decS = Math.max(95, Math.min(130, (int)(Math.min(w, h) * 0.15f)));
            int trX = w - decS - 14;
            int trY = 12;
            AffineTransform savedT = g2.getTransform();
            g2.translate(trX + decS / 2.0, trY + decS / 2.0);
            g2.rotate(Math.toRadians(8));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
            g2.drawImage(imgAdesivoScrapbook, -decS / 2, -decS / 2, decS, decS, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2.setTransform(savedT);
            desenharFitaScrapbook(g2, trX + decS / 2 - 24, trY - 5, 48, 14, new Color(247, 168, 184), 0.10);
        }

        if (layout.sideSize >= 96) {
            // Movimento suave de respiração/flutuação estilo papel artesanal
            int floatPolaroid = (int)(Math.sin(animTicks * 2.2) * 2.8);
            int floatPostit = (int)(Math.sin(animTicks * 2.2 + 1.3) * 2.8);

            // Cartão scrapbook Polaroid à esquerda, equilibrando o Post-it
            int stickerOffsetY = -(int)(layout.sideSize * 0.22) + floatPolaroid;
            desenharStickerCluster(g2, layout.sideLeftX - 15, layout.sideY + stickerOffsetY,
                    layout.sideSize, layout.sideSize);
            desenharFitaScrapbook(g2, layout.sideLeftX - 15 + layout.sideSize / 2 - 22,
                    layout.sideY + stickerOffsetY + 1, 44, 13, new Color(241, 173, 189), -0.10);

            // Post-it à direita
            int postitOffsetY = (int)(layout.sideSize * 0.10) + floatPostit;
            desenharPostItRealistaNovo(g2, layout.sideRightX + 10, layout.sideY + postitOffsetY,
                    layout.sideSize, layout.sideSize);
        }

        // Aura dourada suave cintilante ao redor do título
        float titleGlow = (float)(0.75 + 0.25 * Math.sin(animTicks * 2.4));
        g2.setColor(new Color(255, 222, 110, (int)(40 * titleGlow)));
        g2.fillRoundRect(layout.titleX - 10, layout.titleY - 8, layout.titleW + 20, layout.titleH + 16, 24, 24);

        // Enfeites artísticos de scrapbook emoldurando o título
        desenharWashiTapePoas(g2, layout.titleX - 10, layout.titleY - 6, 44, 13, new Color(255, 225, 140, 220), -14);
        desenharWashiTapePoas(g2, layout.titleX + layout.titleW - 34, layout.titleY - 6, 44, 13, new Color(248, 175, 192, 220), 14);
        desenharBrilhoCintilante(g2, layout.titleX - 18, layout.titleY + layout.titleH / 2, 8, new Color(255, 215, 75));
        desenharBrilhoCintilante(g2, layout.titleX + layout.titleW + 18, layout.titleY + layout.titleH / 2, 8, new Color(255, 215, 75));

        desenharTituloHobbyQuestRecorte(g2, layout.titleX, layout.titleY, layout.titleW, layout.titleH);

        String formHeader = modoCadastro ? "CRIAR CONTA" : "LOGIN";
        // Enfeites delicados ao lado da faixa
        desenharFlorComFolhas(g2, layout.stripX - 20, layout.stripY + layout.stripH / 2, 9, new Color(175, 220, 190), false, 0);
        desenharFlorComFolhas(g2, layout.stripX + layout.stripW + 20, layout.stripY + layout.stripH / 2, 9, new Color(248, 175, 192), false, 0);
        desenharFaixaRasgadaLogin(g2, layout.stripX, layout.stripY, layout.stripW, layout.stripH, formHeader);

        if (modoCadastro) {
            desenharInputPilula(g2, layout.inputX, layout.inputY(0), layout.inputW, layout.inputH,
                    "NOME DE AVENTUREIRO", txtNomeCadastro.getText().isEmpty(), false, false);
            desenharInputPilula(g2, layout.inputX, layout.inputY(1), layout.inputW, layout.inputH,
                    "EMAIL", txtEmail.getText().isEmpty(), false, false);
            desenharInputPilula(g2, layout.inputX, layout.inputY(2), layout.inputW, layout.inputH,
                    "SENHA", txtSenha.getPassword().length == 0, true, senhaVisivel);
            desenharInputPilula(g2, layout.inputX, layout.inputY(3), layout.inputW, layout.inputH,
                    "CONFIRMAR SENHA", txtConfirmaSenha.getPassword().length == 0, true, confirmaSenhaVisivel);
        } else {
            desenharInputPilula(g2, layout.inputX, layout.inputY(0), layout.inputW, layout.inputH,
                    "EMAIL", txtEmail.getText().isEmpty(), false, false);
            desenharInputPilula(g2, layout.inputX, layout.inputY(1), layout.inputW, layout.inputH,
                    "SENHA", txtSenha.getPassword().length == 0, true, senhaVisivel);
            desenharLinkEsqueciSenha(g2, layout.centerX, layout.forgotY);
        }

        // Estrelas e brilhos dourados ao lado do botão ENTRAR/CADASTRAR
        desenharBrilhoCintilante(g2, layout.submitX - 18, layout.submitY + layout.submitH / 2, 9, new Color(255, 215, 75));
        desenharEstrela(g2, layout.submitX - 38, layout.submitY + layout.submitH / 2, 10, 12);
        desenharBrilhoCintilante(g2, layout.submitX + layout.submitW + 18, layout.submitY + layout.submitH / 2, 9, new Color(255, 215, 75));
        desenharEstrela(g2, layout.submitX + layout.submitW + 38, layout.submitY + layout.submitH / 2, 10, -12);

        desenharBotaoEntrarCosturado(g2, layout.submitX, layout.submitY, layout.submitW, layout.submitH,
                modoCadastro ? "CADASTRAR" : "ENTRAR");

        // 4. Partículas mágicas de brilho e coraçõezinhos flutuantes
        desenharParticulasMagicas(g2, w, h);
    }

    private void desenharTituloHobbyQuestRecorte(Graphics2D g2, int x, int y, int w, int h) {
        if (imgTituloHobbyQuest != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            double imgAspect = (double) imgTituloHobbyQuest.getWidth() / imgTituloHobbyQuest.getHeight();
            int drawH = h;
            int drawW = (int) Math.round(drawH * imgAspect);
            if (drawW > w) {
                drawW = w;
                drawH = (int) Math.round(drawW / imgAspect);
            }
            int drawX = x + (w - drawW) / 2;
            int drawY = y + (h - drawH) / 2;

            g2.drawImage(imgTituloHobbyQuest, drawX, drawY, drawW, drawH, null);
            return;
        }

        GeneralPath cut = new GeneralPath();
        cut.moveTo(x + 10, y);
        for (int i = 10; i <= w - 10; i += 15) {
            cut.lineTo(x + i, y + ((i % 30 == 0) ? -2 : 2));
        }
        cut.lineTo(x + w, y + 10);
        cut.lineTo(x + w + 1, y + h - 10);
        for (int i = w - 10; i >= 10; i -= 15) {
            cut.lineTo(x + i, y + h + ((i % 30 == 0) ? 2 : -2));
        }
        cut.lineTo(x, y + h - 10);
        cut.closePath();

        AffineTransform st = AffineTransform.getTranslateInstance(3, 4);
        g2.setColor(new Color(0, 0, 0, 30));
        g2.fill(st.createTransformedShape(cut));

        GradientPaint kraftFundo = new GradientPaint(x, y, new Color(228, 206, 178), x, y + h, new Color(212, 188, 158));
        g2.setPaint(kraftFundo);
        g2.fill(cut);
        g2.setColor(new Color(185, 160, 130, 140));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(cut);

        g2.setFont(fontTitulo.deriveFont(42f));
        FontMetrics fm = g2.getFontMetrics();
        String tHobby = "HOBBY ";
        String tQuest = "QUEST";
        int wHobby = fm.stringWidth(tHobby);
        int wQuest = fm.stringWidth(tQuest);
        int startX = x + (w - (wHobby + wQuest)) / 2;
        int textY = y + h / 2 + 15;

        g2.setColor(Color.WHITE);
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                if (dx != 0 || dy != 0) g2.drawString(tHobby, startX + dx, textY + dy);
            }
        }
        g2.setColor(new Color(185, 172, 218));
        g2.drawString(tHobby, startX, textY);

        int qX = startX + wHobby;
        g2.setColor(Color.WHITE);
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                if (dx != 0 || dy != 0) g2.drawString(tQuest, qX + dx, textY + dy);
            }
        }
        g2.setColor(new Color(148, 186, 142));
        g2.drawString(tQuest, qX, textY);
    }

    private void desenharFaixaRasgadaLogin(Graphics2D g2, int x, int y, int w, int h, String formHeader) {
        if ("LOGIN".equals(formHeader) && imgFaixaLogin != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setColor(new Color(0, 0, 0, 22));
            g2.fillRoundRect(x + 2, y + 3, w, h, 8, 8);
            g2.drawImage(imgFaixaLogin, x, y, w, h, null);
            return;
        }

        GeneralPath strip = new GeneralPath();
        strip.moveTo(x + 4, y + 4);
        for (int i = 4; i <= w - 4; i += 8) {
            strip.lineTo(x + i, y + ((i % 16 == 0) ? -2 : 2));
        }
        strip.lineTo(x + w, y + h - 4);
        for (int i = w - 4; i >= 4; i -= 8) {
            strip.lineTo(x + i, y + h + ((i % 16 == 0) ? 2 : -2));
        }
        strip.closePath();

        AffineTransform st = AffineTransform.getTranslateInstance(2, 3);
        g2.setColor(new Color(0, 0, 0, 22));
        g2.fill(st.createTransformedShape(strip));

        GradientPaint faixaPaper = new GradientPaint(x, y, new Color(242, 238, 230), x, y + h, new Color(228, 222, 212));
        g2.setPaint(faixaPaper);
        g2.fill(strip);

        g2.setColor(new Color(210, 202, 192, 170));
        g2.setStroke(new BasicStroke(0.9f));
        g2.draw(strip);

        g2.setFont(fontCabin.deriveFont(Font.BOLD, 22f));
        g2.setColor(new Color(88, 70, 56));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(formHeader);
        g2.drawString(formHeader, x + (w - tw) / 2, y + h / 2 + 8);
    }

    private void desenharInputPilula(Graphics2D g2, int x, int y, int w, int h, String placeholder, boolean vazio, boolean comOlho, boolean olhoVisivel) {
        g2.setColor(new Color(0, 0, 0, 25));
        g2.fillRoundRect(x + 2, y + 3, w, h, h, h);

        RoundRectangle2D pillShape = new RoundRectangle2D.Float(x, y, w, h, h, h);

        if (imgInputVerde != null) {
            Shape clipAntigo = g2.getClip();
            g2.clip(pillShape);
            g2.drawImage(imgInputVerde, x, y, w, h, null);
            g2.setClip(clipAntigo);
        } else {
            GradientPaint greenBase = new GradientPaint(x, y, new Color(168, 198, 134), x, y + h, new Color(146, 178, 114));
            g2.setPaint(greenBase);
            g2.fill(pillShape);
        }

        g2.setColor(new Color(130, 165, 100, 160));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(x, y, w, h, h, h);

        // Brilho superior suave estilo pílula
        g2.setColor(new Color(255, 255, 255, 45));
        g2.fillRoundRect(x + 4, y + 2, w - 8, h / 3, h / 3, h / 3);

        // Ícone indicativo fofo no canto esquerdo da pílula
        int icX = x + 18;
        int icY = y + h / 2;
        g2.setColor(new Color(75, 108, 55, 210));
        g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (placeholder.contains("EMAIL")) {
            // Cartinha / envelope
            g2.drawRoundRect(icX, icY - 6, 15, 11, 3, 3);
            g2.drawLine(icX + 1, icY - 5, icX + 7, icY - 1);
            g2.drawLine(icX + 14, icY - 5, icX + 7, icY - 1);
        } else if (placeholder.contains("SENHA")) {
            // Cadeadinho
            g2.drawRoundRect(icX + 2, icY - 2, 12, 10, 3, 3);
            g2.drawArc(icX + 4, icY - 8, 8, 8, 0, 180);
            g2.fillOval(icX + 7, icY + 2, 2, 2);
        } else if (placeholder.contains("AVENTUREIRO") || placeholder.contains("NOME")) {
            // Estrelinha mágica
            desenharEstrela(g2, icX + 8, icY, 6, 0);
        }

        if (vazio) {
            g2.setColor(new Color(75, 105, 55, 210));
            g2.setFont(fontCabin.deriveFont(Font.BOLD, 17f));
            FontMetrics fmp = g2.getFontMetrics();
            int pw = fmp.stringWidth(placeholder);
            g2.drawString(placeholder, x + (w - pw) / 2, y + h / 2 + 6);
        }

        if (comOlho) {
            int eyeX = x + w - 38;
            int eyeY = y + h / 2;
            float eyeScale = 1.0f + 0.18f * hoverOlhoAnim;

            AffineTransform oldEye = g2.getTransform();
            g2.translate(eyeX + 11, eyeY);
            g2.scale(eyeScale, eyeScale);
            g2.translate(-(eyeX + 11), -eyeY);

            Color eyeColor = hoverOlhoAnim > 0.1f ? new Color(55, 42, 28) : new Color(105, 92, 72);
            g2.setColor(eyeColor);
            g2.setStroke(new BasicStroke(hoverOlhoAnim > 0.1f ? 2.4f : 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(eyeX, eyeY - 8, 22, 16, 0, 180);
            g2.drawArc(eyeX, eyeY - 8, 22, 16, 180, 180);
            if (olhoVisivel) {
                g2.fillOval(eyeX + 8, eyeY - 3, 6, 6);
            } else {
                g2.fillOval(eyeX + 8, eyeY - 3, 6, 6);
                g2.drawLine(eyeX - 2, eyeY + 10, eyeX + 24, eyeY - 10);
            }
            g2.setTransform(oldEye);
        }
    }

    private void desenharLinkEsqueciSenha(Graphics2D g2, int centerX, int y) {
        float hAnim = hoverEsqueciAnim;
        float liftY = -3.0f * hAnim;
        float scale = 1.0f + 0.065f * hAnim;

        int tagW = 160, tagH = 34;
        int tagX = centerX - tagW / 2;
        int tagY = y - tagH / 2;

        AffineTransform old = g2.getTransform();
        g2.translate(centerX, y + liftY);
        g2.scale(scale, scale);
        g2.translate(-centerX, -y);

        int shadowAlpha = Math.min(50, (int)(18 + 16 * hAnim));
        g2.setColor(new Color(0, 0, 0, shadowAlpha));
        g2.fillRect(tagX + 2, tagY + 2 + (int)(3 * hAnim), tagW, tagH);

        if (hAnim > 0.05f) {
            g2.setColor(new Color(255, 255, 255, (int)(120 * hAnim)));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRect(tagX - 1, tagY - 1, tagW + 2, tagH + 2);
        }

        if (imgFaixaEsqueciSenha != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(imgFaixaEsqueciSenha, tagX, tagY, tagW, tagH, null);
            g2.setTransform(old);
            return;
        }

        String esq = "ESQUECI A SENHA";
        g2.setFont(fontCabin.deriveFont(Font.BOLD, 13f));
        FontMetrics fm = g2.getFontMetrics();
        int ew = fm.stringWidth(esq);

        int padH = 6, padW = 14;
        tagW = ew + padW * 2;
        tagH = fm.getHeight() + padH * 2;
        tagX = centerX - tagW / 2;
        tagY = y - fm.getAscent() - padH;

        g2.setColor(new Color(242, 236, 226));
        g2.fillRect(tagX, tagY, tagW, tagH);
        g2.setColor(new Color(218, 208, 194));
        g2.setStroke(new BasicStroke(0.9f));
        g2.drawRect(tagX, tagY, tagW, tagH);

        g2.setColor(hAnim > 0.1f ? new Color(55, 40, 25) : new Color(88, 72, 58));
        g2.drawString(esq, tagX + padW, y);

        g2.setTransform(old);
    }

    private void desenharBotaoEntrarCosturado(Graphics2D g2, int x, int y, int w, int h, String texto) {
        boolean isClicked = (clickRect == btnSubmitAuth && clickScale != 1.0f);
        float hAnim = hoverSubmitAnim;

        // Efeito de hover: botao levanta 4px e expande 5.5%, clique contrai
        float currentScale = isClicked ? clickScale : (1.0f + 0.055f * hAnim);
        float liftY = isClicked ? 0 : (-4.0f * hAnim);

        AffineTransform old = g2.getTransform();
        g2.translate(x + w / 2.0, y + h / 2.0 + liftY);
        g2.scale(currentScale, currentScale);
        g2.translate(-(x + w / 2.0), -(y + h / 2.0));

        // Brilho / glow suave dourado ao passar o mouse
        if (hAnim > 0.05f) {
            g2.setColor(new Color(255, 235, 170, (int)(75 * hAnim)));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRoundRect(x - 2, y - 2, w + 4, h + 4, h + 4, h + 4);
        }

        if ("ENTRAR".equals(texto) && imgBtnEntrar != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(imgBtnEntrar, x, y, w, h, null);
        } else {
            Color topKraft = hAnim > 0 ? new Color(212, 172, 130) : new Color(196, 156, 116);
            Color botKraft = hAnim > 0 ? new Color(188, 146, 104) : new Color(172, 132, 92);
            GradientPaint kraft = new GradientPaint(x, y, topKraft, x, y + h, botKraft);
            g2.setPaint(kraft);
            g2.fillRoundRect(x, y, w, h, h, h);

            g2.setColor(new Color(255, 255, 255, (int)(40 + 35 * hAnim)));
            g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, h - 2, h - 2);

            g2.setColor(new Color(92, 60, 36));
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, new float[]{5, 4}, 0));
            g2.drawRoundRect(x + 5, y + 5, w - 10, h - 10, h - 10, h - 10);

            g2.setFont(fontCabin.deriveFont(Font.BOLD, 22f));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(texto);
            int tx = x + (w - tw) / 2;
            int ty = y + h / 2 + 8;

            g2.setColor(new Color(255, 240, 220, 90));
            g2.drawString(texto, tx + 1, ty + 1);
            g2.setColor(new Color(75, 46, 24));
            g2.drawString(texto, tx, ty);
        }

        // Efeito Shimmer — varredura diagonal de luz brilhante animada
        float shimmerProgress = (animTicks * 0.30f) % 2.6f;
        if (shimmerProgress < 1.0f) {
            Shape clipAntigo = g2.getClip();
            g2.clip(new RoundRectangle2D.Float(x, y, w, h, h, h));
            int shimX = (int)(x - 40 + shimmerProgress * (w + 80));
            GradientPaint shimGrad = new GradientPaint(
                shimX - 25, y, new Color(255, 255, 255, 0),
                shimX, y, new Color(255, 255, 255, 110),
                true
            );
            g2.setPaint(shimGrad);
            g2.fillRect(shimX - 30, y, 60, h);
            g2.setClip(clipAntigo);
        }

        g2.setTransform(old);
    }

    private void desenharPostItRealistaNovo(Graphics2D g2, int x, int y, int w, int h) {
        boolean isClicked = (clickRect == btnPostItToggle && clickScale != 1.0f);
        float hAnim = hoverPostItAnim;
        float currentScale = isClicked ? clickScale : (1.0f + 0.045f * hAnim);
        float liftY = isClicked ? 0 : (-3.5f * hAnim);
        double angleDeg = 1.5 + (Math.sin(animTicks * 4.0) * 1.5 * hAnim);

        AffineTransform old = g2.getTransform();
        g2.translate(x + w / 2.0, y + h / 2.0 + liftY);
        g2.rotate(Math.toRadians(angleDeg));
        g2.scale(currentScale, currentScale);
        g2.translate(-(w / 2.0), -(h / 2.0));

        // Sombra com projecao dinamica no hover
        int shadowY = 5 + (int)(4 * hAnim);
        int shadowAlpha = Math.min(55, (int)(22 + 15 * hAnim));
        g2.setColor(new Color(0, 0, 0, shadowAlpha));
        g2.fillRect(4, shadowY, w, h);

        if (imgPostItAlfinete != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(imgPostItAlfinete, 0, 0, w, h, null);
        } else {
            // Papel do Post-it creme/amarelado suave artesanal
            GradientPaint postitPaper = new GradientPaint(0, 0, new Color(255, 252, 238), w, h, new Color(248, 242, 222));
            g2.setPaint(postitPaper);
            g2.fillRect(0, 0, w, h);

            // Dobrinha sutil no canto inferior direito
            int curlS = 14;
            g2.setColor(new Color(0, 0, 0, 16));
            g2.fillPolygon(new int[]{w - curlS - 2, w, w}, new int[]{h, h - curlS - 2, h}, 3);
            g2.setColor(new Color(238, 228, 202));
            g2.fillPolygon(new int[]{w - curlS, w, w}, new int[]{h, h - curlS, h}, 3);

            g2.setColor(new Color(225, 216, 195));
            g2.setStroke(new BasicStroke(0.9f));
            g2.drawRect(0, 0, w, h);

            int pinX = w / 2;
            int pinY = 12;
            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillOval(pinX - 5, pinY + 6, 12, 6);
            GradientPaint pinHead = new GradientPaint(pinX - 7, pinY - 7, new Color(245, 75, 65), pinX + 7, pinY + 7, new Color(175, 30, 25));
            g2.setPaint(pinHead);
            g2.fillOval(pinX - 7, pinY - 7, 14, 14);
            g2.setColor(new Color(140, 20, 15));
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawOval(pinX - 7, pinY - 7, 14, 14);
            g2.setColor(new Color(255, 255, 255, 230));
            g2.fillOval(pinX - 4, pinY - 5, 4, 4);
        }

        // Texto do Post-it (caneta esferografica sobre o papel)
        g2.setColor(new Color(85, 75, 65));
        g2.setFont(fontCabin.deriveFont(Font.BOLD, 13.5f));
        FontMetrics fm = g2.getFontMetrics();

        String l1 = modoCadastro ? "JA TEM UMA" : "NAO TEM UMA";
        String l2 = "CONTA";
        String l3 = modoCadastro ? "FAZER LOGIN" : "CRIAR CONTA";

        g2.drawString(l1, (w - fm.stringWidth(l1)) / 2, 54);
        g2.drawString(l2, (w - fm.stringWidth(l2)) / 2, 74);

        Color corDestaque = hAnim > 0.1f ? new Color(35, 145, 25) : new Color(70, 120, 60);
        g2.setColor(corDestaque);
        float fontTam = 15f + 1.0f * hAnim;
        g2.setFont(fontCabin.deriveFont(Font.BOLD, fontTam));
        FontMetrics fmB = g2.getFontMetrics();
        g2.drawString(l3, (w - fmB.stringWidth(l3)) / 2, 104);

        // Coraçãozinho fofo no canto do post-it
        desenharCoracaoRabisco(g2, w - 20, h - 18, 8, new Color(224, 122, 95, 160));

        g2.setTransform(old);
    }

    private void desenharTachinhaDourada(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(0, 0, 0, 45));
        g2.fillOval(x - 6, y + 4, 12, 8);
        GradientPaint pinGrad = new GradientPaint(x - 7, y - 7, new Color(225, 195, 110), x + 7, y + 7, new Color(160, 130, 50));
        g2.setPaint(pinGrad);
        g2.fillOval(x - 7, y - 7, 14, 14);
        g2.setColor(new Color(130, 105, 40));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawOval(x - 7, y - 7, 14, 14);
        g2.setColor(new Color(255, 245, 180));
        g2.fillOval(x - 4, y - 5, 4, 4);
    }

    private void desenharStickerCluster(Graphics2D g2, int x, int y, int w, int h) {
        if (imgAdesivoScrapbook != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            AffineTransform old = g2.getTransform();
            g2.translate(x + w / 2.0, y + h / 2.0);
            g2.rotate(Math.toRadians(-4.0));

            double imgAspect = (double) imgAdesivoScrapbook.getWidth() / imgAdesivoScrapbook.getHeight();
            int drawH = h;
            int drawW = (int) Math.round(drawH * imgAspect);
            if (drawW > w) {
                drawW = w;
                drawH = (int) Math.round(drawW / imgAspect);
            }
            int drawX = -drawW / 2;
            int drawY = -drawH / 2;

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.96f));
            g2.drawImage(imgAdesivoScrapbook, drawX, drawY, drawW, drawH, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            g2.setTransform(old);
            return;
        }
    }

    private void desenharFitaScrapbook(Graphics2D g2, int x, int y, int w, int h, Color cor, double angulo) {
        Graphics2D fita = (Graphics2D)g2.create();
        fita.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        fita.translate(x + w / 2.0, y + h / 2.0);
        fita.rotate(Math.toRadians(angulo));
        fita.translate(-w / 2.0, -h / 2.0);
        fita.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.78f));
        fita.setColor(new Color(80, 65, 50, 25));
        fita.fillRoundRect(1, 2, w, h, 5, 5);
        fita.setPaint(new GradientPaint(0, 0, cor.brighter(), 0, h, cor));
        fita.fillRoundRect(0, 0, w, h, 5, 5);
        fita.setColor(new Color(255, 255, 255, 105));
        fita.setStroke(new BasicStroke(1f));
        for (int stripeX = 5; stripeX < w; stripeX += 9) {
            fita.drawLine(stripeX, h - 2, stripeX + 5, 2);
        }
        fita.dispose();
    }

    /**
     * Efeito 1: Varal de bandeirolas de scrapbook pastel decorando o topo.
     */
    private void desenharVaralBandeirolas(Graphics2D g2, int w, int h) {
        Color[] coresBandeirolas = {
            new Color(255, 185, 195, 210), // rosa pastel
            new Color(255, 230, 150, 210), // amarelo sol
            new Color(175, 225, 175, 210), // menta suave
            new Color(170, 210, 245, 210), // azul céu
            new Color(215, 185, 235, 210)  // lavanda
        };

        // 1. Varal do topo esquerdo (curvado suave)
        int x1 = 10, y1 = 6;
        int x2 = (int)(w * 0.24), y2 = 20;
        g2.setColor(new Color(145, 130, 115, 160));
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        QuadCurve2D cordaEsq = new QuadCurve2D.Float(x1, y1, (x1 + x2) / 2f, y1 + 18, x2, y2);
        g2.draw(cordaEsq);

        for (int i = 0; i < 5; i++) {
            float t = (i + 0.5f) / 5.0f;
            float px = (1 - t) * (1 - t) * x1 + 2 * (1 - t) * t * ((x1 + x2) / 2f) + t * t * x2;
            float py = (1 - t) * (1 - t) * y1 + 2 * (1 - t) * t * (y1 + 18) + t * t * y2;
            int bw = 16, bh = 20;
            Polygon flag = new Polygon(
                new int[]{(int)px - bw / 2, (int)px + bw / 2, (int)px},
                new int[]{(int)py, (int)py, (int)py + bh},
                3
            );
            g2.setColor(coresBandeirolas[i % coresBandeirolas.length]);
            g2.fillPolygon(flag);
            g2.setColor(new Color(255, 255, 255, 180));
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawPolygon(flag);
            g2.fillOval((int)px - 1, (int)py + 5, 3, 3);
        }

        // 2. Varal do topo direito (curvado suave)
        int rx1 = (int)(w * 0.74), ry1 = 20;
        int rx2 = w - 10, ry2 = 6;
        QuadCurve2D cordaDir = new QuadCurve2D.Float(rx1, ry1, (rx1 + rx2) / 2f, ry1 + 18, rx2, ry2);
        g2.setColor(new Color(145, 130, 115, 160));
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(cordaDir);

        for (int i = 0; i < 5; i++) {
            float t = (i + 0.5f) / 5.0f;
            float px = (1 - t) * (1 - t) * rx1 + 2 * (1 - t) * t * ((rx1 + rx2) / 2f) + t * t * rx2;
            float py = (1 - t) * (1 - t) * ry1 + 2 * (1 - t) * t * (ry1 + 18) + t * t * ry2;
            int bw = 16, bh = 20;
            Polygon flag = new Polygon(
                new int[]{(int)px - bw / 2, (int)px + bw / 2, (int)px},
                new int[]{(int)py, (int)py, (int)py + bh},
                3
            );
            g2.setColor(coresBandeirolas[(i + 2) % coresBandeirolas.length]);
            g2.fillPolygon(flag);
            g2.setColor(new Color(255, 255, 255, 180));
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawPolygon(flag);
            g2.fillOval((int)px - 1, (int)py + 5, 3, 3);
        }
    }

    /**
     * Efeito 2: Luz solar da manhã suave entrando diagonalmente pelo canto superior direito.
     */
    private void desenharEfeitoLuzESol(Graphics2D g2, int w, int h) {
        Composite oldComp = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));

        int startX = (int)(w * 0.82);
        for (int i = 0; i < 3; i++) {
            Polygon ray = new Polygon(
                new int[]{startX + i * 45, startX + i * 45 + 55, (int)(w * 0.35) + i * 85, (int)(w * 0.25) + i * 85},
                new int[]{0, 0, h, h},
                4
            );
            float pulse = (float)(0.75 + 0.25 * Math.sin(animTicks * 1.5 + i * 1.1));
            g2.setColor(new Color(255, 248, 215, (int)(18 * pulse)));
            g2.fillPolygon(ray);
        }

        g2.setComposite(oldComp);
    }

    /**
     * Efeito 3: Partículas de brilho mágico e coraçõezinhos flutuantes animados.
     */
    private void desenharParticulasMagicas(Graphics2D g2, int w, int h) {
        Composite oldComp = g2.getComposite();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 26 partículas flutuantes calculadas dinamicamente via animTicks
        for (int i = 0; i < 26; i++) {
            float seed = i * 37.17f;
            float speed = 14f + (i % 6) * 6f;
            float totalH = h + 100f;
            float curY = (totalH - ((animTicks * speed + seed * 23f) % totalH)) - 40;
            float sway = (float)(Math.sin(animTicks * 1.4 + seed) * 18);
            float curX = (float)((seed * 89.3f) % (w - 40) + 20 + sway);

            float alpha = (float)(0.35 + 0.45 * Math.sin(animTicks * 2.2 + seed));
            alpha = Math.max(0f, Math.min(1f, alpha));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            if (i % 3 == 0) {
                // Brilho estelar ✦
                g2.setColor(new Color(255, 220, 80));
                int sz = 4 + (i % 3);
                g2.drawLine((int)curX - sz, (int)curY, (int)curX + sz, (int)curY);
                g2.drawLine((int)curX, (int)curY - sz, (int)curX, (int)curY + sz);
                g2.setColor(Color.WHITE);
                g2.fillOval((int)curX - 1, (int)curY - 1, 2, 2);
            } else if (i % 3 == 1) {
                // Mini coraçãozinho flutuante ♡
                g2.setColor(new Color(255, 170, 190));
                int hs = 5;
                g2.fillOval((int)curX - hs, (int)curY - hs, hs, hs);
                g2.fillOval((int)curX, (int)curY - hs, hs, hs);
                Polygon tri = new Polygon(
                    new int[]{(int)curX - hs, (int)curX + hs, (int)curX},
                    new int[]{(int)curY - 1, (int)curY - 1, (int)curY + hs + 1},
                    3
                );
                g2.fillPolygon(tri);
            } else {
                // Mote de pólen / poeira estelar cintilante
                g2.setColor(new Color(255, 240, 160));
                int pr = 2 + (i % 2);
                g2.fillOval((int)curX - pr, (int)curY - pr, pr * 2, pr * 2);
            }
        }

        g2.setComposite(oldComp);
    }

    private void desenharDoodlesFundoExatos(Graphics2D g2, int w, int h) {
        AffineTransform old;

        // ═══════════════════════════════════════════════════════════════
        // 🌸 1. CANTO SUPERIOR ESQUERDO
        // ═══════════════════════════════════════════════════════════════
        // Washi tape rosa chiclete com listras
        old = g2.getTransform();
        g2.translate((int)(w * 0.035), (int)(h * 0.045));
        g2.rotate(Math.toRadians(-12));
        g2.setColor(new Color(248, 175, 192, 180));
        g2.fillRect(0, 0, 75, 16);
        g2.setColor(new Color(255, 255, 255, 120));
        g2.setStroke(new BasicStroke(1.2f));
        for (int sx = 6; sx < 75; sx += 10) g2.drawLine(sx, 16, sx + 5, 0);
        g2.setTransform(old);

        // Laço de fita decorativo sobre a washi tape
        desenharLacoDeFita(g2, (int)(w * 0.038), (int)(h * 0.038), 10, new Color(255, 160, 185));

        desenharClipDePapelColorido(g2, (int)(w * 0.105), (int)(h * 0.025), new Color(108, 152, 205, 220));
        desenharFlorComFolhas(g2,       (int)(w * 0.075), (int)(h * 0.115), 14, new Color(248, 170, 190), true, -0.4);
        desenharEstrela(g2,             (int)(w * 0.140), (int)(h * 0.065), 12, 12);
        desenharBrilhoCintilante(g2,    (int)(w * 0.040), (int)(h * 0.155), 8, new Color(255, 215, 80));
        desenharPetalaSolta(g2,         (int)(w * 0.110), (int)(h * 0.145), 7, new Color(255, 190, 205), 35);
        desenharRaminhoFolhas(g2,       (int)(w * 0.022), (int)(h * 0.190), -22);
        desenharConfetesMagicos(g2,     (int)(w * 0.065), (int)(h * 0.180));

        // Abelhinha fofa voando perto das flores
        desenharAbelhinhaFofa(g2,       (int)(w * 0.135), (int)(h * 0.130), -15);

        // ═══════════════════════════════════════════════════════════════
        // 🌿 2. LADO ESQUERDO SUPERIOR & MÉDIO
        // ═══════════════════════════════════════════════════════════════
        desenharFlorComFolhas(g2,       (int)(w * 0.060), (int)(h * 0.235), 13, new Color(195, 175, 235), false, 0);
        desenharEspiralDoodle(g2,       (int)(w * 0.115), (int)(h * 0.220), 12, new Color(82, 163, 133, 190));
        desenharNotasMusicais(g2,       (int)(w * 0.080), (int)(h * 0.260), new Color(145, 130, 195, 180));
        desenharCoracaoRabisco(g2,      (int)(w * 0.050), (int)(h * 0.320), 15, new Color(225, 120, 125, 190));
        desenharBrilhoCintilante(g2,    (int)(w * 0.095), (int)(h * 0.300), 7, new Color(255, 205, 70));
        desenharTracosVerdesArtisticos(g2, (int)(w * 0.040), (int)(h * 0.380));

        // Suculenta e mini lápis fofos
        desenharPlantinhaSuculenta(g2,  (int)(w * 0.045), (int)(h * 0.440), 24);
        desenharPaletaArtista(g2,       (int)(w * 0.078), (int)(h * 0.415), 36);
        desenharMiniLapis(g2,           (int)(w * 0.112), (int)(h * 0.460), -35);
        desenharConfetesMagicos(g2,     (int)(w * 0.045), (int)(h * 0.510));

        // ═══════════════════════════════════════════════════════════════
        // 🌻 3. LADO ESQUERDO INFERIOR
        // ═══════════════════════════════════════════════════════════════
        desenharWashiTapePoas(g2,       (int)(w * 0.035), (int)(h * 0.580), 55, 13, new Color(185, 220, 240, 190), -8);
        desenharFlorComFolhas(g2,       (int)(w * 0.070), (int)(h * 0.640), 14, new Color(255, 215, 120), true, 0.5);
        desenharPetalaSolta(g2,         (int)(w * 0.110), (int)(h * 0.610), 6, new Color(255, 225, 140), -20);
        desenharNuvemDoodle(g2,         (int)(w * 0.125), (int)(h * 0.705), 44, 22);
        desenharEstrela(g2,             (int)(w * 0.055), (int)(h * 0.755), 12, 16);
        desenharBrilhoCintilante(g2,    (int)(w * 0.100), (int)(h * 0.775), 8, new Color(255, 195, 75));
        desenharClipDePapelColorido(g2, (int)(w * 0.050), (int)(h * 0.835), new Color(175, 145, 215, 220));
        desenharXicaraFofa(g2,          (int)(w * 0.088), (int)(h * 0.835), 28, 25);
        desenharConfetesMagicos(g2,     (int)(w * 0.055), (int)(h * 0.890));

        // ═══════════════════════════════════════════════════════════════
        // ✨ 4. TOPO CENTRAL (emoldurando o título com leveza)
        // ═══════════════════════════════════════════════════════════════
        desenharBrilhoCintilante(g2,    (int)(w * 0.310), (int)(h * 0.050), 9, new Color(255, 215, 75));
        desenharEstrela(g2,             (int)(w * 0.365), (int)(h * 0.040), 9, 8);
        desenharFlorComFolhas(g2,       (int)(w * 0.230), (int)(h * 0.065), 11, new Color(165, 220, 190), false, 0);
        desenharPetalaSolta(g2,         (int)(w * 0.270), (int)(h * 0.080), 6, new Color(175, 225, 200), 40);
        desenharConfetesMagicos(g2,     (int)(w * 0.490), (int)(h * 0.038));
        desenharPetalaSolta(g2,         (int)(w * 0.730), (int)(h * 0.080), 6, new Color(255, 205, 190), -30);
        desenharFlorComFolhas(g2,       (int)(w * 0.770), (int)(h * 0.065), 11, new Color(255, 195, 175), false, 0);
        desenharEstrela(g2,             (int)(w * 0.635), (int)(h * 0.040), 9, -8);
        desenharBrilhoCintilante(g2,    (int)(w * 0.690), (int)(h * 0.050), 9, new Color(255, 215, 75));

        // ═══════════════════════════════════════════════════════════════
        // 🌸 5. CANTO SUPERIOR DIREITO
        // ═══════════════════════════════════════════════════════════════
        // Washi tape verde menta com listras
        old = g2.getTransform();
        g2.translate((int)(w * 0.840), (int)(h * 0.055));
        g2.rotate(Math.toRadians(14));
        g2.setColor(new Color(135, 195, 80, 180));
        g2.fillRect(0, 0, 72, 16);
        g2.setColor(new Color(255, 255, 255, 110));
        g2.setStroke(new BasicStroke(1.2f));
        for (int sx = 5; sx < 72; sx += 9) g2.drawLine(sx, 0, sx + 5, 16);
        g2.setTransform(old);

        desenharRaminhoFolhas(g2,       (int)(w * 0.965), (int)(h * 0.045), 22);
        desenharFlorComFolhas(g2,       (int)(w * 0.915), (int)(h * 0.090), 15, new Color(165, 140, 215), true, 0.6);
        desenharEstrela(g2,             (int)(w * 0.855), (int)(h * 0.135), 12, -15);
        desenharBrilhoCintilante(g2,    (int)(w * 0.945), (int)(h * 0.170), 8, new Color(255, 215, 75));
        desenharCoracaoRabisco(g2,      (int)(w * 0.890), (int)(h * 0.225), 14, new Color(235, 185, 75, 190));
        desenharNotasMusicais(g2,       (int)(w * 0.925), (int)(h * 0.275), new Color(215, 155, 185, 180));
        desenharConfetesMagicos(g2,     (int)(w * 0.865), (int)(h * 0.310));

        // ═══════════════════════════════════════════════════════════════
        // 🌷 6. LADO DIREITO MÉDIO (ao redor do post-it)
        // ═══════════════════════════════════════════════════════════════
        // Peça de quebra-cabeça pastel (celebrando o hobby Games / Puzzle)
        desenharPecaQuebraCabeca(g2,    (int)(w * 0.890), (int)(h * 0.360), 26);
        desenharFlorComFolhas(g2,       (int)(w * 0.935), (int)(h * 0.430), 13, new Color(248, 170, 190), false, 0);
        desenharEspiralDoodle(g2,       (int)(w * 0.875), (int)(h * 0.485), 11, new Color(235, 155, 190, 170));
        desenharEstrela(g2,             (int)(w * 0.940), (int)(h * 0.540), 11, 20);
        desenharPlantinhaSuculenta(g2,  (int)(w * 0.945), (int)(h * 0.585), 22);
        desenharSetaCurvada(g2,         (int)(w * 0.875), (int)(h * 0.615), new Color(80, 175, 85, 185));
        desenharPetalaSolta(g2,         (int)(w * 0.920), (int)(h * 0.640), 7, new Color(255, 185, 205), 15);

        // ═══════════════════════════════════════════════════════════════
        // 📖 7. LADO DIREITO INFERIOR
        // ═══════════════════════════════════════════════════════════════
        desenharWashiTapePoas(g2,       (int)(w * 0.895), (int)(h * 0.680), 55, 13, new Color(245, 195, 215, 180), 10);
        desenharFlorComFolhas(g2,       (int)(w * 0.925), (int)(h * 0.725), 14, new Color(135, 200, 225), true, -0.5);
        desenharLivroAberto(g2,         (int)(w * 0.850), (int)(h * 0.775), 44, 30);
        desenharClipDePapelColorido(g2, (int)(w * 0.940), (int)(h * 0.835), new Color(235, 185, 80, 220));
        desenharMiniLapis(g2,           (int)(w * 0.945), (int)(h * 0.870), 38);
        desenharFlorComFolhas(g2,       (int)(w * 0.815), (int)(h * 0.890), 12, new Color(255, 205, 140), false, 0);

        // Aviãozinho de papel decolando suavemente
        desenharAviaozinhoPapel(g2,     (int)(w * 0.875), (int)(h * 0.885), -25);
        desenharBrilhoCintilante(g2,    (int)(w * 0.935), (int)(h * 0.915), 8, new Color(248, 160, 185));

        // ═══════════════════════════════════════════════════════════════
        // ☁️ 8. FUNDO / BASE (delicado e harmonioso)
        // ═══════════════════════════════════════════════════════════════
        desenharNuvemDoodle(g2,         (int)(w * 0.470), (int)(h * 0.920), 46, 22);
        desenharFlorComFolhas(g2,       (int)(w * 0.355), (int)(h * 0.930), 11, new Color(255, 215, 120), false, 0);
        desenharFlorComFolhas(g2,       (int)(w * 0.625), (int)(h * 0.930), 11, new Color(248, 175, 192), false, 0);
        desenharPetalaSolta(g2,         (int)(w * 0.280), (int)(h * 0.940), 6, new Color(255, 205, 140), 60);
        desenharPetalaSolta(g2,         (int)(w * 0.700), (int)(h * 0.940), 6, new Color(245, 185, 205), -45);
        desenharBrilhoCintilante(g2,    (int)(w * 0.490), (int)(h * 0.905), 7, new Color(255, 215, 75));
        desenharEstrela(g2,             (int)(w * 0.420), (int)(h * 0.940), 8, 6);
        desenharEstrela(g2,             (int)(w * 0.560), (int)(h * 0.940), 8, -8);
        desenharConfetesMagicos(g2,     (int)(w * 0.490), (int)(h * 0.945));
    }

    private void desenharAbelhinhaFofa(Graphics2D g2, int x, int y, double angulo) {
        AffineTransform old = g2.getTransform();
        g2.translate(x, y);
        g2.rotate(Math.toRadians(angulo));

        // Rastro de voo pontilhado
        g2.setColor(new Color(130, 120, 105, 130));
        g2.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{3, 4}, 0));
        GeneralPath trail = new GeneralPath();
        trail.moveTo(-28, 12);
        trail.quadTo(-16, -6, -4, 4);
        g2.draw(trail);

        // Asinhas transparentes fofas
        g2.setColor(new Color(230, 245, 255, 180));
        g2.fillOval(-3, -12, 8, 11);
        g2.fillOval(3, -13, 7, 10);
        g2.setColor(new Color(150, 190, 220, 190));
        g2.setStroke(new BasicStroke(0.8f));
        g2.drawOval(-3, -12, 8, 11);
        g2.drawOval(3, -13, 7, 10);

        // Corpinho amarelo listrado
        g2.setColor(new Color(255, 215, 60));
        g2.fillOval(-8, -5, 16, 11);
        // Listrinhas pretas
        g2.setColor(new Color(60, 45, 30));
        g2.fillRoundRect(-3, -5, 3, 11, 2, 2);
        g2.fillRoundRect(2, -4, 3, 9, 2, 2);
        // Contorno
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawOval(-8, -5, 16, 11);
        // Bochecha rosada
        g2.setColor(new Color(255, 160, 175));
        g2.fillOval(-7, 0, 3, 3);
        // Anteninhas
        g2.setColor(new Color(60, 45, 30));
        g2.drawLine(-7, -4, -11, -8);
        g2.drawLine(-5, -5, -7, -9);

        g2.setTransform(old);
    }

    private void desenharAviaozinhoPapel(Graphics2D g2, int x, int y, double angulo) {
        AffineTransform old = g2.getTransform();
        g2.translate(x, y);
        g2.rotate(Math.toRadians(angulo));

        // Trajeto tracejado em curva
        g2.setColor(new Color(140, 160, 180, 140));
        g2.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{3, 4}, 0));
        GeneralPath loop = new GeneralPath();
        loop.moveTo(-35, 18);
        loop.curveTo(-25, 30, -5, 25, -15, 10);
        loop.curveTo(-22, -2, -10, -4, 0, 0);
        g2.draw(loop);

        // Aviãozinho dobrado em papel
        Polygon asaDir = new Polygon(new int[]{0, 18, 0}, new int[]{0, -3, 8}, 3);
        Polygon asaEsq = new Polygon(new int[]{0, 18, -4}, new int[]{0, -3, -6}, 3);
        Polygon vinco = new Polygon(new int[]{0, 18, 0}, new int[]{0, -3, 2}, 3);

        g2.setColor(new Color(255, 255, 255, 230));
        g2.fillPolygon(asaDir);
        g2.setColor(new Color(240, 240, 245, 230));
        g2.fillPolygon(asaEsq);
        g2.setColor(new Color(225, 225, 235, 230));
        g2.fillPolygon(vinco);

        g2.setColor(new Color(110, 135, 160, 210));
        g2.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawPolygon(asaDir);
        g2.drawPolygon(asaEsq);

        g2.setTransform(old);
    }

    private void desenharLacoDeFita(Graphics2D g2, int x, int y, int size, Color cor) {
        AffineTransform old = g2.getTransform();
        g2.translate(x, y);

        int s = size;
        g2.setColor(cor);
        GeneralPath asaE = new GeneralPath();
        asaE.moveTo(0, 0);
        asaE.curveTo(-s, -s, -s * 1.2, s * 0.4, 0, s * 0.2);
        asaE.closePath();
        g2.fill(asaE);

        GeneralPath asaD = new GeneralPath();
        asaD.moveTo(0, 0);
        asaD.curveTo(s, -s, s * 1.2, s * 0.4, 0, s * 0.2);
        asaD.closePath();
        g2.fill(asaD);

        GeneralPath pontas = new GeneralPath();
        pontas.moveTo(-2, (float)(s * 0.2));
        pontas.quadTo(-s * 0.6, s * 0.8, -s * 0.4, (float)(s * 1.3));
        pontas.moveTo(2, (float)(s * 0.2));
        pontas.quadTo(s * 0.6, s * 0.8, s * 0.4, (float)(s * 1.3));
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(pontas);

        g2.fillOval(-s / 4, -s / 5, s / 2, (int)(s * 0.45));
        g2.setColor(new Color(255, 255, 255, 120));
        g2.fillOval(-s / 6, -s / 6, s / 3, (int)(s * 0.25));

        g2.setColor(new Color(Math.max(0, cor.getRed() - 30), Math.max(0, cor.getGreen() - 30), Math.max(0, cor.getBlue() - 30), 160));
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(asaE);
        g2.draw(asaD);

        g2.setTransform(old);
    }

    private void desenharNotasMusicais(Graphics2D g2, int x, int y, Color cor) {
        AffineTransform old = g2.getTransform();
        g2.translate(x, y);
        g2.setColor(cor);

        // Nota dupla
        g2.fillOval(0, 8, 6, 5);
        g2.fillOval(12, 6, 6, 5);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(5, 10, 5, 0);
        g2.drawLine(17, 8, 17, -2);
        g2.drawLine(5, 0, 17, -2);

        // Nota simples menor
        g2.fillOval(24, 2, 5, 4);
        g2.drawLine(28, 4, 28, -5);
        g2.drawLine(28, -5, 32, -3);

        g2.setTransform(old);
    }

    private void desenharPetalaSolta(Graphics2D g2, int x, int y, int size, Color cor, double angulo) {
        AffineTransform old = g2.getTransform();
        g2.translate(x, y);
        g2.rotate(Math.toRadians(angulo));

        g2.setColor(cor);
        GeneralPath petala = new GeneralPath();
        petala.moveTo(0, -size);
        petala.curveTo(size * 0.8, -size * 0.4, size * 0.6, size * 0.8, 0, size);
        petala.curveTo(-size * 0.6, size * 0.8, -size * 0.8, -size * 0.4, 0, -size);
        petala.closePath();
        g2.fill(petala);

        Color borda = new Color(Math.max(0, cor.getRed() - 35), Math.max(0, cor.getGreen() - 35), Math.max(0, cor.getBlue() - 35), 160);
        g2.setColor(borda);
        g2.setStroke(new BasicStroke(0.9f));
        g2.draw(petala);

        g2.setTransform(old);
    }

    private void desenharWashiTapePoas(Graphics2D g2, int x, int y, int w, int h, Color cor, double angulo) {
        AffineTransform old = g2.getTransform();
        g2.translate(x + w / 2.0, y + h / 2.0);
        g2.rotate(Math.toRadians(angulo));
        g2.translate(-w / 2.0, -h / 2.0);

        g2.setColor(cor);
        g2.fillRoundRect(0, 0, w, h, 4, 4);

        g2.setColor(new Color(255, 255, 255, 150));
        for (int px = 6; px < w - 4; px += 10) {
            g2.fillOval(px, h / 2 - 2, 4, 4);
        }

        g2.setTransform(old);
    }

    private void desenharConfetesMagicos(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(248, 175, 192, 190)); // rosa
        g2.fillOval(x, y, 4, 4);
        g2.setColor(new Color(255, 215, 80, 190));  // dourado
        g2.fillOval(x + 7, y - 5, 3, 3);
        g2.setColor(new Color(165, 220, 190, 190)); // menta
        g2.fillOval(x + 5, y + 6, 4, 4);
    }

    private void desenharPaletaArtista(Graphics2D g2, int x, int y, int size) {
        g2.setColor(new Color(215, 204, 200));
        g2.fillOval(x, y, size, (int)(size * 0.75));
        g2.setColor(new Color(160, 140, 135));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawOval(x, y, size, (int)(size * 0.75));

        // Furo do polegar
        g2.setColor(COLOR_BG_PAGE);
        g2.fillOval(x + size - 11, y + (int)(size * 0.35), 7, 7);

        // Manchas de tinta
        g2.setColor(new Color(239, 83, 80));
        g2.fillOval(x + 5, y + 4, 6, 6);
        g2.setColor(new Color(255, 202, 40));
        g2.fillOval(x + 13, y + 3, 6, 6);
        g2.setColor(new Color(66, 165, 245));
        g2.fillOval(x + 6, y + 12, 6, 6);

        // Pincel
        AffineTransform bT = g2.getTransform();
        g2.translate(x + size / 2, y + size / 2);
        g2.rotate(Math.toRadians(-45));
        g2.setColor(new Color(141, 110, 99));
        g2.fillRect(-2, -18, 4, 30);
        g2.setColor(new Color(171, 71, 188));
        g2.fillRoundRect(-3, -24, 6, 8, 3, 3);
        g2.setTransform(bT);
    }

    private void desenharPecaQuebraCabeca(Graphics2D g2, int x, int y, int size) {
        g2.setColor(new Color(129, 175, 200, 200));
        g2.fillRoundRect(x, y, size, size, 8, 8);
        g2.fillOval(x + size - 4, y + size / 2 - 5, 10, 10);
        g2.setColor(COLOR_BG_PAGE);
        g2.fillOval(x + size / 2 - 5, y + size - 5, 10, 10);
        g2.setColor(new Color(90, 135, 160, 220));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x, y, size, size, 8, 8);
    }

    private void desenharLivroAberto(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(93, 64, 55, 180));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x + w / 2, y + 4, x + w / 2, y + h);
        g2.drawRoundRect(x, y + 2, w / 2, h - 2, 4, 4);
        for (int ly = y + 8; ly < y + h - 6; ly += 5) {
            g2.drawLine(x + 5, ly, x + w / 2 - 5, ly);
        }
        g2.drawRoundRect(x + w / 2, y + 2, w / 2, h - 2, 4, 4);
        for (int ly = y + 8; ly < y + h - 6; ly += 5) {
            g2.drawLine(x + w / 2 + 5, ly, x + w - 5, ly);
        }
    }

    private void desenharPlantinhaSuculenta(Graphics2D g2, int x, int y, int size) {
        AffineTransform old = g2.getTransform();
        g2.translate(x, y);

        int potW = size;
        int potH = (int)(size * 0.72);
        Polygon vaso = new Polygon(
            new int[]{(int)(potW * 0.15), (int)(potW * 0.85), (int)(potW * 0.75), (int)(potW * 0.25)},
            new int[]{potH / 3, potH / 3, potH, potH},
            4
        );
        g2.setColor(new Color(235, 155, 130));
        g2.fillPolygon(vaso);
        g2.setColor(new Color(185, 105, 80));
        g2.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawPolygon(vaso);

        g2.setColor(new Color(245, 170, 145));
        g2.fillRoundRect((int)(potW * 0.10), potH / 4 - 3, (int)(potW * 0.80), 7, 3, 3);
        g2.setColor(new Color(185, 105, 80));
        g2.drawRoundRect((int)(potW * 0.10), potH / 4 - 3, (int)(potW * 0.80), 7, 3, 3);

        int cx = potW / 2;
        int cy = potH / 4;
        g2.setColor(new Color(140, 195, 145));
        g2.fillOval(cx - 5, cy - 14, 10, 15);
        g2.fillOval(cx - 12, cy - 10, 10, 13);
        g2.fillOval(cx + 2, cy - 10, 10, 13);

        g2.setColor(new Color(85, 145, 95));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawOval(cx - 5, cy - 14, 10, 15);
        g2.drawOval(cx - 12, cy - 10, 10, 13);
        g2.drawOval(cx + 2, cy - 10, 10, 13);

        g2.setColor(new Color(255, 255, 255, 180));
        g2.fillOval((int)(potW * 0.3), (int)(potH * 0.5), 3, 3);

        g2.setTransform(old);
    }

    private void desenharXicaraFofa(Graphics2D g2, int x, int y, int w, int h) {
        AffineTransform old = g2.getTransform();
        g2.translate(x, y);

        g2.setColor(new Color(252, 205, 220));
        g2.fillRoundRect(0, h / 3, (int)(w * 0.78), (int)(h * 0.65), 8, 8);
        g2.setColor(new Color(215, 140, 160));
        g2.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawRoundRect(0, h / 3, (int)(w * 0.78), (int)(h * 0.65), 8, 8);

        g2.drawArc((int)(w * 0.65), (int)(h * 0.42), (int)(w * 0.32), (int)(h * 0.45), -90, 180);

        g2.setColor(new Color(230, 115, 145));
        int mx = (int)(w * 0.39);
        int my = (int)(h * 0.65);
        g2.fillOval(mx - 4, my - 3, 4, 4);
        g2.fillOval(mx, my - 3, 4, 4);
        g2.fillPolygon(new int[]{mx - 4, mx + 4, mx}, new int[]{my, my, my + 5}, 3);

        g2.setColor(new Color(160, 150, 145, 140));
        g2.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GeneralPath steam = new GeneralPath();
        steam.moveTo(mx, h / 3 - 3);
        steam.quadTo(mx - 4, h / 4, mx, h / 7);
        steam.curveTo(mx - 5, h / 12, mx - 5, 0, mx, 2);
        steam.curveTo(mx + 5, 0, mx + 5, h / 12, mx, h / 7);
        g2.draw(steam);

        g2.setTransform(old);
    }

    private void desenharMiniLapis(Graphics2D g2, int x, int y, double angulo) {
        AffineTransform old = g2.getTransform();
        g2.translate(x, y);
        g2.rotate(Math.toRadians(angulo));

        g2.setColor(new Color(255, 215, 75));
        g2.fillRect(0, 0, 8, 28);
        g2.setColor(new Color(215, 165, 30));
        g2.setStroke(new BasicStroke(0.9f));
        g2.drawRect(0, 0, 8, 28);

        g2.setColor(new Color(255, 175, 190));
        g2.fillRoundRect(0, -7, 8, 7, 3, 3);
        g2.setColor(new Color(195, 195, 205));
        g2.fillRect(0, -2, 8, 4);

        Polygon ponta = new Polygon(new int[]{0, 8, 4}, new int[]{28, 28, 38}, 3);
        g2.setColor(new Color(245, 222, 179));
        g2.fillPolygon(ponta);
        g2.setColor(new Color(185, 155, 115));
        g2.drawPolygon(ponta);

        Polygon grafite = new Polygon(new int[]{2, 6, 4}, new int[]{34, 34, 38}, 3);
        g2.setColor(new Color(65, 65, 75));
        g2.fillPolygon(grafite);

        g2.setTransform(old);
    }

    private void desenharRaminhoFolhas(Graphics2D g2, int x, int y, double angulo) {
        AffineTransform old = g2.getTransform();
        g2.translate(x, y);
        g2.rotate(Math.toRadians(angulo));

        g2.setColor(new Color(110, 160, 115, 210));
        g2.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GeneralPath caule = new GeneralPath();
        caule.moveTo(0, 30);
        caule.curveTo(4, 18, -3, 8, 2, -4);
        g2.draw(caule);

        g2.setColor(new Color(145, 205, 155, 210));
        g2.fillOval(-8, 18, 8, 5);
        g2.fillOval(2, 13, 8, 5);
        g2.fillOval(-9, 6, 8, 5);
        g2.fillOval(1, 1, 8, 5);
        g2.fillOval(-3, -7, 6, 7);

        g2.setColor(new Color(90, 145, 95, 200));
        g2.setStroke(new BasicStroke(0.8f));
        g2.drawOval(-8, 18, 8, 5);
        g2.drawOval(2, 13, 8, 5);
        g2.drawOval(-9, 6, 8, 5);
        g2.drawOval(1, 1, 8, 5);
        g2.drawOval(-3, -7, 6, 7);

        g2.setTransform(old);
    }

    private void desenharFlorDoodleColorida(Graphics2D g2, int cx, int cy, int r, Color cor) {
        desenharFlorComFolhas(g2, cx, cy, r, cor, false, 0);
    }

    private void desenharFlorComFolhas(Graphics2D g2, int cx, int cy, int r, Color cor, boolean folhas, double angFolha) {
        AffineTransform old = g2.getTransform();
        g2.translate(cx, cy);

        // Folhinhas verdes delicadas
        if (folhas) {
            AffineTransform leafT = g2.getTransform();
            g2.rotate(angFolha);
            // Folha 1
            g2.setColor(new Color(135, 185, 130, 210));
            g2.fillOval(-r - 4, -r / 4, (int)(r * 0.95), (int)(r * 0.55));
            g2.setColor(new Color(90, 145, 85, 190));
            g2.setStroke(new BasicStroke(0.9f));
            g2.drawOval(-r - 4, -r / 4, (int)(r * 0.95), (int)(r * 0.55));
            // Folha 2
            g2.setColor(new Color(148, 198, 142, 210));
            g2.fillOval(-r / 3, r / 2, (int)(r * 0.85), (int)(r * 0.50));
            g2.setColor(new Color(90, 145, 85, 190));
            g2.drawOval(-r / 3, r / 2, (int)(r * 0.85), (int)(r * 0.50));
            g2.setTransform(leafT);
        }

        // Sombra suave sob as pétalas
        g2.setColor(new Color(0, 0, 0, 15));
        for (int i = 0; i < 5; i++) {
            double ang = i * 2 * Math.PI / 5;
            int px = (int)(Math.cos(ang) * r * 0.55) + 1;
            int py = (int)(Math.sin(ang) * r * 0.55) + 2;
            g2.fillOval(px - r / 3, py - r / 3, r * 2 / 3, r * 2 / 3);
        }

        // Pétalas com preenchimento e contorno refinado
        Color corBorda = new Color(
            Math.max(0, cor.getRed() - 40),
            Math.max(0, cor.getGreen() - 40),
            Math.max(0, cor.getBlue() - 40),
            Math.min(255, cor.getAlpha() + 40)
        );

        for (int i = 0; i < 5; i++) {
            double ang = i * 2 * Math.PI / 5;
            int px = (int)(Math.cos(ang) * r * 0.55);
            int py = (int)(Math.sin(ang) * r * 0.55);
            g2.setColor(cor);
            g2.fillOval(px - r / 3, py - r / 3, r * 2 / 3, r * 2 / 3);
            g2.setColor(corBorda);
            g2.setStroke(new BasicStroke(1.1f));
            g2.drawOval(px - r / 3, py - r / 3, r * 2 / 3, r * 2 / 3);
        }

        // Miolo dourado da flor
        g2.setColor(new Color(255, 215, 80));
        g2.fillOval(-r / 4, -r / 4, r / 2, r / 2);
        g2.setColor(new Color(210, 155, 30));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawOval(-r / 4, -r / 4, r / 2, r / 2);

        // Pontinho de brilho branco
        g2.setColor(new Color(255, 255, 255, 220));
        g2.fillOval(-r / 8, -r / 8, Math.max(2, r / 5), Math.max(2, r / 5));

        g2.setTransform(old);
    }

    private void desenharBrilhoCintilante(Graphics2D g2, int cx, int cy, int size, Color cor) {
        AffineTransform old = g2.getTransform();
        g2.translate(cx, cy);

        // Pulsação suave em tempo real
        float pulse = (float)(0.84 + 0.28 * Math.sin(animTicks * 3.2 + (cx * 0.04 + cy * 0.06)));
        g2.scale(pulse, pulse);

        // Halo suave ao redor com brilho
        g2.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), (int)(52 * Math.min(1.0f, pulse))));
        g2.fillOval(-size - 2, -size - 2, (size + 2) * 2, (size + 2) * 2);

        // 4 raios curvos em cruz estilo sparkle ✦
        g2.setColor(cor);
        GeneralPath spark = new GeneralPath();
        spark.moveTo(0, -size);
        spark.quadTo(0, 0, size, 0);
        spark.quadTo(0, 0, 0, size);
        spark.quadTo(0, 0, -size, 0);
        spark.quadTo(0, 0, 0, -size);
        spark.closePath();
        g2.fill(spark);

        // Ponto central branco cintilante
        g2.setColor(Color.WHITE);
        g2.fillOval(-2, -2, 4, 4);

        g2.setTransform(old);
    }

    private void desenharClipDePapelColorido(Graphics2D g2, int x, int y, Color cor) {
        g2.setColor(cor);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawRoundRect(x, y, 12, 38, 6, 6);
        g2.drawRoundRect(x + 3, y - 6, 6, 30, 4, 4);
    }

    // ==========================================
    // DOODLES DE FUNDO PARA TELAS INTERNAS (LOGADO)
    // ==========================================
    private void desenharDoodlesFundoLogado(Graphics2D g2, int cx, int w, int h) {
        // Enfeites sutis nos cantos e bordas das telas internas
        // sem cobrir o conteúdo central

        // Cantos superiores
        desenharFlorComFolhas(g2, cx + 18, 18, 10, new Color(248, 185, 210), false, 0.3);
        desenharBrilhoCintilante(g2, cx + 50, 32, 7, new Color(255, 215, 75));
        desenharFlorComFolhas(g2, cx + CONTAINER_W - 28, 20, 10, new Color(165, 215, 185), false, -0.3);
        desenharBrilhoCintilante(g2, cx + CONTAINER_W - 56, 35, 7, new Color(255, 215, 75));

        // Estrelinhas espalhadas (animadas)
        desenharEstrela(g2, cx + 30, 70, 7, animTicks * 1.2 % 360);
        desenharEstrela(g2, cx + CONTAINER_W - 36, 68, 7, -(animTicks * 1.5 % 360));

        // Flores e folhas nas bordas laterais (canto inferior esquerdo)
        desenharFlorComFolhas(g2, cx + 12, h - 100, 11, new Color(255, 185, 195), false, 0.4);
        desenharFlorComFolhas(g2, cx + CONTAINER_W - 18, h - 96, 11, new Color(195, 230, 175), false, -0.3);

        // Notas musicais e corações flutuantes discretos
        Composite oldC = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.38f));
        desenharNotasMusicais(g2, cx + CONTAINER_W - 48, 110, new Color(195, 165, 230));
        desenharCoracaoRabisco(g2, cx + 36, 110, 10, new Color(255, 165, 185, 180));
        g2.setComposite(oldC);

        // Mini brilhos cintilantes espalhados no fundo
        int[] bx = {cx + 80, cx + CONTAINER_W - 90, cx + 55, cx + CONTAINER_W - 60};
        int[] by = {155, 165, h - 140, h - 145};
        Color[] bCores = {new Color(255, 215, 75), new Color(255, 185, 200), new Color(175, 235, 190), new Color(195, 185, 240)};
        for (int i = 0; i < 4; i++) {
            desenharBrilhoCintilante(g2, bx[i], by[i], 6, bCores[i]);
        }
    }

    // ==========================================
    // ENFEITES DECORATIVOS LATERAIS
    // ==========================================
    private void desenharEnfeitesLaterais(Graphics2D g2, int cx, int w, int h) {
        int margemEsq = cx; // largura da margem esquerda
        int margemDir = w - (cx + CONTAINER_W); // largura da margem direita

        // ===== LADO ESQUERDO =====

        // Washi tape rosa inclinada (topo esquerdo)
        AffineTransform old = g2.getTransform();
        g2.translate(15, 60);
        g2.rotate(Math.toRadians(-12));
        g2.setColor(new Color(255, 182, 193, 160));
        g2.fillRect(0, 0, 90, 18);
        // Listras na fita
        g2.setColor(new Color(255, 255, 255, 50));
        g2.setStroke(new BasicStroke(0.5f));
        for (int i = 0; i < 90; i += 5) {
            g2.drawLine(i, 0, i + 3, 18);
        }
        g2.setTransform(old);

        // Coração desenhado à mão (esquerda, meio)
        desenharCoracaoRabisco(g2, 45, 200, 28, new Color(224, 122, 95, 150));

        // Florzinha doodle (esquerda, abaixo)
        desenharFlorzinhaDoodle(g2, 55, 340, 18, new Color(241, 160, 196, 180));

        // Nuvenzinha fofa (esquerda, meio-baixo)
        desenharNuvemDoodle(g2, 35, 460, 50, 25);

        // Clip de papel (esquerda, embaixo)
        desenharClipDePapel(g2, 70, 560, new Color(91, 155, 213, 160));

        // Estrelinha pequena dourada (esquerda)
        desenharEstrela(g2, 50, 640, 12, 15);

        // Espiral doodle (esquerda, topo-meio)
        desenharEspiralDoodle(g2, 60, 130, 15, new Color(103, 167, 96, 120));

        // ===== LADO DIREITO =====

        // Washi tape verde inclinada (direita, acima do meio)
        old = g2.getTransform();
        g2.translate(cx + CONTAINER_W + margemDir - 95, 250);
        g2.rotate(Math.toRadians(15));
        g2.setColor(new Color(168, 214, 72, 140));
        g2.fillRect(0, 0, 85, 16);
        g2.setColor(new Color(255, 255, 255, 45));
        g2.setStroke(new BasicStroke(0.5f));
        for (int i = 0; i < 85; i += 5) {
            g2.drawLine(i, 0, i + 2, 16);
        }
        g2.setTransform(old);

        // Coração (direita, acima)
        desenharCoracaoRabisco(g2, cx + CONTAINER_W + margemDir / 2, 150, 22, new Color(226, 183, 64, 160));

        // Florzinha (direita, meio)
        desenharFlorzinhaDoodle(g2, cx + CONTAINER_W + margemDir / 2, 400, 16, new Color(157, 116, 194, 160));

        // Seta curvada doodle (direita)
        desenharSetaCurvada(g2, cx + CONTAINER_W + margemDir / 2 - 15, 500, new Color(103, 167, 96, 130));

        // Clip de papel (direita, embaixo)
        desenharClipDePapel(g2, cx + CONTAINER_W + margemDir / 2 - 10, 590, new Color(224, 122, 95, 150));

        // Estrelinhas decorativas (direita)
        desenharEstrela(g2, cx + CONTAINER_W + margemDir / 2, 70, 14, -10);
        desenharEstrela(g2, cx + CONTAINER_W + margemDir / 2 + 25, 660, 10, 20);

        // Espiral (direita)
        desenharEspiralDoodle(g2, cx + CONTAINER_W + margemDir / 2 + 10, 310, 12, new Color(226, 183, 64, 130));
    }

    private void desenharCoracaoRabisco(Graphics2D g2, int cx, int cy, int tamanho, Color cor) {
        g2.setColor(cor);
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GeneralPath corac = new GeneralPath();
        int s = tamanho;
        corac.moveTo(cx, cy + s / 3);
        corac.curveTo(cx - s, cy - s / 2, cx - s / 4, cy - s, cx, cy - s / 3);
        corac.curveTo(cx + s / 4, cy - s, cx + s, cy - s / 2, cx, cy + s / 3);
        corac.closePath();
        g2.draw(corac);
        // Preenchimento suave
        g2.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), cor.getAlpha() / 3));
        g2.fill(corac);
    }

    private void desenharFlorzinhaDoodle(Graphics2D g2, int cx, int cy, int r, Color cor) {
        // Pétalas em círculo
        g2.setColor(cor);
        for (int i = 0; i < 6; i++) {
            double ang = i * Math.PI / 3;
            int px = (int)(cx + Math.cos(ang) * r * 0.6);
            int py = (int)(cy + Math.sin(ang) * r * 0.6);
            g2.fillOval(px - r / 3, py - r / 3, r * 2 / 3, r * 2 / 3);
        }
        // Centro amarelo
        g2.setColor(new Color(252, 226, 139, 220));
        g2.fillOval(cx - r / 4, cy - r / 4, r / 2, r / 2);
        // Contorno fino das pétalas
        g2.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), cor.getAlpha() / 2));
        g2.setStroke(new BasicStroke(1.0f));
        for (int i = 0; i < 6; i++) {
            double ang = i * Math.PI / 3;
            int px = (int)(cx + Math.cos(ang) * r * 0.6);
            int py = (int)(cy + Math.sin(ang) * r * 0.6);
            g2.drawOval(px - r / 3, py - r / 3, r * 2 / 3, r * 2 / 3);
        }
    }

    private void desenharNuvemDoodle(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(200, 215, 230, 80));
        // Corpo da nuvem com ovais sobrepostas
        g2.fillOval(x, y + h / 3, w / 2, h * 2 / 3);
        g2.fillOval(x + w / 4, y, w / 2, h * 2 / 3);
        g2.fillOval(x + w / 2, y + h / 4, w / 2, h * 2 / 3);
        // Contorno
        g2.setColor(new Color(170, 195, 220, 100));
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawOval(x, y + h / 3, w / 2, h * 2 / 3);
        g2.drawOval(x + w / 4, y, w / 2, h * 2 / 3);
        g2.drawOval(x + w / 2, y + h / 4, w / 2, h * 2 / 3);
    }

    private void desenharClipDePapel(Graphics2D g2, int x, int y, Color cor) {
        g2.setColor(cor);
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Forma de clip (retângulo alongado com pontas arredondadas)
        g2.drawRoundRect(x, y, 14, 45, 8, 8);
        g2.drawRoundRect(x + 3, y - 8, 8, 35, 6, 6);
    }

    private void desenharSetaCurvada(Graphics2D g2, int x, int y, Color cor) {
        g2.setColor(cor);
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Curva da seta
        GeneralPath seta = new GeneralPath();
        seta.moveTo(x, y);
        seta.curveTo(x + 25, y - 20, x + 35, y + 10, x + 15, y + 30);
        g2.draw(seta);
        // Ponta da seta
        g2.drawLine(x + 15, y + 30, x + 22, y + 24);
        g2.drawLine(x + 15, y + 30, x + 10, y + 23);
    }

    private void desenharEspiralDoodle(Graphics2D g2, int cx, int cy, int r, Color cor) {
        g2.setColor(cor);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GeneralPath espiral = new GeneralPath();
        boolean first = true;
        for (double t = 0; t < 4 * Math.PI; t += 0.15) {
            double raio = r * 0.15 + (r * 0.85 * t / (4 * Math.PI));
            double px = cx + Math.cos(t) * raio;
            double py = cy + Math.sin(t) * raio;
            if (first) { espiral.moveTo(px, py); first = false; }
            else espiral.lineTo(px, py);
        }
        g2.draw(espiral);
    }

    private void desenharBotaoOlho(Graphics2D g2, int x, int y, boolean visivel, Rectangle hitArea) {
        int size = 32;
        hitArea.setBounds(x, y, size, size);

        // Fundo circular sutil
        g2.setColor(new Color(0, 0, 0, 18));
        g2.fillOval(x + 2, y + 2, size, size);
        g2.setColor(new Color(255, 255, 255, 180));
        g2.fillOval(x, y, size, size);

        // Borda suave
        g2.setColor(new Color(103, 167, 96, 100));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawOval(x, y, size, size);

        int cx = x + size / 2;
        int cy = y + size / 2;

        // Desenhar ícone de olho
        g2.setColor(COLOR_DARK_GREEN);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Forma do olho (arco superior e inferior)
        GeneralPath olho = new GeneralPath();
        olho.moveTo(cx - 9, cy);
        olho.curveTo(cx - 5, cy - 6, cx + 5, cy - 6, cx + 9, cy);
        olho.curveTo(cx + 5, cy + 6, cx - 5, cy + 6, cx - 9, cy);
        olho.closePath();
        g2.draw(olho);

        // Pupila (íris)
        g2.fillOval(cx - 3, cy - 3, 6, 6);

        // Brilho na pupila
        g2.setColor(Color.WHITE);
        g2.fillOval(cx - 1, cy - 2, 2, 2);

        // Se a senha está oculta, desenhar barra diagonal sobre o olho
        if (!visivel) {
            g2.setColor(COLOR_CORAL);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(cx - 8, cy + 7, cx + 8, cy - 7);
        }
    }

    private void desenharPostItRealista(Graphics2D g2, int x, int y) {
        AffineTransform old = g2.getTransform();
        g2.translate(x, y);
        g2.rotate(Math.toRadians(8));

        // Sombra difusa
        g2.setColor(new Color(0, 0, 0, 22));
        g2.fillRect(5, 6, 112, 86);
        g2.setColor(new Color(0, 0, 0, 12));
        g2.fillRect(6, 7, 112, 86);

        // Bloco Amarelo do Post-it
        g2.setColor(COLOR_POSTIT_YELLOW);
        g2.fillRect(0, 0, 112, 86);

        // Borda sutil
        g2.setColor(new Color(230, 205, 110, 180));
        g2.setStroke(new BasicStroke(0.8f));
        g2.drawRect(0, 0, 112, 86);

        // Dobra sutil no canto inferior direito
        g2.setColor(new Color(0, 0, 0, 18));
        Polygon dobra = new Polygon();
        dobra.addPoint(112, 86);
        dobra.addPoint(112, 70);
        dobra.addPoint(95, 86);
        g2.fillPolygon(dobra);

        // Alfinete Vermelho 3D
        desenharAlfinete3D(g2, 50, -12);

        // Texto do Post-it
        g2.setFont(fontCabin.deriveFont(11.5f));
        g2.setColor(new Color(55, 55, 55));
        if (modoCadastro) {
            g2.drawString("JÁ TEM UMA", 18, 30);
            g2.drawString("CONTA?", 32, 46);
            g2.setFont(fontCabin.deriveFont(Font.BOLD, 12f));
            g2.setColor(COLOR_DARK_GREEN);
            g2.drawString("FAZER LOGIN", 15, 66);
        } else {
            g2.drawString("NÃO TEM UMA", 14, 30);
            g2.drawString("CONTA?", 30, 46);
            g2.setFont(fontCabin.deriveFont(Font.BOLD, 12f));
            g2.setColor(COLOR_DARK_GREEN);
            g2.drawString("CRIAR CONTA", 14, 66);
        }

        g2.setTransform(old);
    }

    private void desenharAlfinete3D(Graphics2D g2, int x, int y) {
        // Sombra
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillOval(x + 3, y + 3, 14, 14);
        // Corpo vermelho
        g2.setColor(COLOR_RED_PIN);
        g2.fillOval(x, y, 14, 14);
        // Brilho
        g2.setColor(new Color(255, 130, 120));
        g2.fillOval(x + 3, y + 2, 5, 5);
        // Ponta do alfinete
        g2.setColor(new Color(120, 120, 120));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawLine(x + 7, y + 14, x + 7, y + 20);
    }

    private void desenharCartaoRasgadoLogin(Graphics2D g2, String titulo, int cx, int cy) {
        int cw = 350, ch = 68;
        int x = cx + (CONTAINER_W - cw) / 2;

        // Forma com bordas rasgadas (clip-path simulado)
        GeneralPath torn = new GeneralPath();
        // Topo com irregularidades
        torn.moveTo(x, cy + 4);
        for (int i = 0; i <= cw; i += 12) {
            double jag = (i % 24 == 0) ? -3 : 2;
            torn.lineTo(x + i, cy + jag + (Math.random() * 1.5));
        }
        // Direita
        torn.lineTo(x + cw, cy + ch);
        // Base com irregularidades
        for (int i = cw; i >= 0; i -= 12) {
            double jag = (i % 24 == 0) ? 3 : -2;
            torn.lineTo(x + i, cy + ch + jag);
        }
        torn.closePath();

        // Sombra
        AffineTransform shadowT = AffineTransform.getTranslateInstance(3, 4);
        g2.setColor(new Color(0, 0, 0, 18));
        g2.fill(shadowT.createTransformedShape(torn));

        // Cartão cor cardboard com textura
        g2.setColor(COLOR_CARDBOARD);
        g2.fill(torn);

        // Furos de fichário à esquerda
        for (int i = 0; i < 4; i++) {
            int hy = cy + 10 + (i * 13);
            // Sombra interna do furo
            g2.setColor(new Color(0, 0, 0, 25));
            g2.fillOval(x + 14, hy + 1, 10, 10);
            // Furo
            g2.setColor(COLOR_BG_PAGE);
            g2.fillOval(x + 13, hy, 10, 10);
        }

        // Alfinete no canto esquerdo do cartão
        desenharAlfinete3D(g2, x + 28, cy - 12);

        // Texto do título
        int pbX = x + 50, pbW = cw - 70;
        g2.setColor(COLOR_GREEN_TITLE);
        g2.setFont(fontCabin.deriveFont(30f));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(titulo, pbX + (pbW - fm.stringWidth(titulo)) / 2, cy + 47);
    }

    private void desenharCaixaPincelada(Graphics2D g2, int x, int y, int w, int h, String placeholder, boolean vazio) {
        // Forma orgânica simulando pincelada (bordas irregulares)
        RoundRectangle2D.Float shape = new RoundRectangle2D.Float(x, y, w, h, 
            (float)(h * 0.8), (float)(h * 0.4));
        
        // Sombra suave
        g2.setColor(new Color(0, 0, 0, 15));
        g2.fill(new RoundRectangle2D.Float(x + 2, y + 3, w, h, h * 0.8f, h * 0.4f));
        
        // Preenchimento ocre
        g2.setColor(COLOR_INPUT_OCHRE);
        g2.fill(shape);

        // Placeholder
        if (vazio) {
            g2.setFont(fontCabin.deriveFont(19f));
            g2.setColor(new Color(103, 167, 96, 200));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(placeholder, x + (w - fm.stringWidth(placeholder)) / 2, y + h / 2 + 7);
        }
    }

    private void desenharBotaoSubmitAuth(Graphics2D g2, int cx, int y, String texto) {
        int bw = 220, bh = 55;
        int bx = cx + (CONTAINER_W - bw) / 2;

        // Sombra suave do botão
        g2.setColor(new Color(0, 0, 0, 18));
        g2.fillRoundRect(bx + 3, y + 4, bw, bh, 28, 28);

        // Fundo com gradiente creme/dourado
        GradientPaint btnGrad = new GradientPaint(bx, y, new Color(255, 252, 238), bx, y + bh, new Color(252, 242, 210));
        g2.setPaint(btnGrad);
        g2.fillRoundRect(bx, y, bw, bh, 28, 28);

        // Borda tracejada dourada animada
        g2.setColor(COLOR_GOLD_STAR);
        g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{7, 5}, animTicks * 10));
        g2.drawRoundRect(bx, y, bw, bh, 28, 28);

        // Brilho superior
        g2.setColor(new Color(255, 255, 255, 100));
        g2.fillRoundRect(bx + 4, y + 2, bw - 8, bh / 3, 20, 20);

        // Ícone ✦ antes do texto
        g2.setColor(COLOR_GOLD_STAR);
        g2.setFont(fontCabin.deriveFont(26f));
        FontMetrics fm = g2.getFontMetrics();
        int totalW = fm.stringWidth("✦ " + texto);
        int textX = bx + (bw - totalW) / 2;
        g2.drawString("✦ " + texto, textX, y + bh / 2 + 9);
    }

    private void desenharTracosVerdesArtisticos(Graphics2D g2, int x, int y) {
        g2.setColor(COLOR_GREEN_TITLE);
        g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // 3 tracinhos diagonais como no screenshot
        g2.drawLine(x, y, x + 22, y - 14);
        g2.drawLine(x + 3, y + 18, x + 25, y + 4);
        g2.drawLine(x + 6, y + 36, x + 22, y + 24);
    }

    private void desenharEstrela(Graphics2D g2, int cx, int cy, int r, double angulo) {
        AffineTransform old = g2.getTransform();
        g2.translate(cx, cy);

        // Cintilação dinâmica viva
        float pulse = (float)(0.90 + 0.18 * Math.sin(animTicks * 2.6 + (cx * 0.03 + cy * 0.05)));
        g2.scale(pulse, pulse);

        g2.rotate(Math.toRadians(angulo));

        int points = 4; // Estrela clássica 4 pontas
        int[] xPoints = new int[points * 2];
        int[] yPoints = new int[points * 2];
        for (int i = 0; i < points * 2; i++) {
            double angle = i * Math.PI / points - Math.PI / 2;
            int radius = (i % 2 == 0) ? r : (int)(r * 0.35);
            xPoints[i] = (int) (Math.cos(angle) * radius);
            yPoints[i] = (int) (Math.sin(angle) * radius);
        }

        // Sombra suave e quente
        g2.setColor(new Color(150, 105, 30, 32));
        g2.fillPolygon(ajustarOffset(xPoints, 2), ajustarOffset(yPoints, 2), points * 2);

        // Preenchimento com gradiente dourado quente e radiante
        GradientPaint starGrad = new GradientPaint(
            -r / 2, -r / 2, new Color(255, 245, 175),
             r / 2,  r / 2, new Color(248, 192, 55)
        );
        g2.setPaint(starGrad);
        g2.fillPolygon(xPoints, yPoints, points * 2);

        // Borda dourada refinada
        g2.setColor(new Color(195, 140, 30, 220));
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawPolygon(xPoints, yPoints, points * 2);

        // Brilho central cintilante
        g2.setColor(new Color(255, 255, 255, 235));
        g2.fillOval(-2, -2, 4, 4);

        g2.setTransform(old);
    }

    private int[] ajustarOffset(int[] pts, int off) {
        int[] res = new int[pts.length];
        for (int i = 0; i < pts.length; i++) res[i] = pts[i] + off;
        return res;
    }

    // ==========================================
    // 2. TELA HOME
    // ==========================================
    private void desenharHome(Graphics2D g2, int cx, int w, int h) {
        int CONTAINER_W = getContainerW();

        // Título "HOBBY QUEST" com outline — tamanho proporcional
        String titulo = "HOBBY QUEST";
        float tituloSize = Math.min(56f, Math.max(36f, CONTAINER_W * 0.06f));
        g2.setFont(fontTitulo.deriveFont(tituloSize));
        FontMetrics fmT = g2.getFontMetrics();
        int tx = cx + (CONTAINER_W - fmT.stringWidth(titulo)) / 2;

        // Decorações artísticas ao redor do título
        desenharBrilhoCintilante(g2, tx - 28, 55, 10, new Color(255, 215, 75));
        desenharEstrela(g2, tx - 52, 45, 11, -12);
        desenharFlorComFolhas(g2, tx - 88, 60, 13, new Color(248, 175, 192), true, 0.4);

        desenharBrilhoCintilante(g2, tx + fmT.stringWidth(titulo) + 28, 55, 10, new Color(255, 215, 75));
        desenharEstrela(g2, tx + fmT.stringWidth(titulo) + 52, 45, 11, 15);
        desenharFlorComFolhas(g2, tx + fmT.stringWidth(titulo) + 88, 60, 13, new Color(165, 215, 185), true, -0.4);

        // Aviãozinho de papel no topo esquerdo da Home
        desenharAviaozinhoPapel(g2, cx + 55, 75, 15);

        // Abelhinha fofa voando no topo direito da Home
        desenharAbelhinhaFofa(g2, cx + CONTAINER_W - 50, 70, -20);

        // Outline branco
        g2.setColor(Color.WHITE);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx != 0 || dy != 0) g2.drawString(titulo, tx + dx, 68 + dy);
            }
        }
        g2.setColor(new Color(0, 0, 0, 20));
        g2.drawString(titulo, tx + 2, 71);
        g2.setColor(COLOR_GREEN_TITLE);
        g2.drawString(titulo, tx, 68);

        // Saudação personalizada em itálico
        String primeiroNome = (nomeJogador != null && !nomeJogador.trim().isEmpty())
                ? nomeJogador.trim() : "Aventureiro";
        if (primeiroNome.length() > 1) {
            primeiroNome = Character.toUpperCase(primeiroNome.charAt(0)) + primeiroNome.substring(1);
        }
        g2.setFont(fontCaveat.deriveFont(Font.ITALIC, 22f));
        g2.setColor(new Color(70, 70, 70));
        FontMetrics fmS = g2.getFontMetrics();
        String sub = encurtarTexto("Bem-vindo de volta, " + primeiroNome + "!", fmS, CONTAINER_W - 180);
        int subX = cx + (CONTAINER_W - fmS.stringWidth(sub)) / 2;
        g2.drawString(sub, subX, 105);

        // Enfeites ao lado do subtítulo (notas musicais e coraçãozinho)
        desenharCoracaoRabisco(g2, subX - 26, 102, 12, new Color(248, 160, 180, 190));
        desenharBrilhoCintilante(g2, subX + fmS.stringWidth(sub) + 14, 96, 8, new Color(255, 215, 75));
        desenharNotasMusicais(g2, subX + fmS.stringWidth(sub) + 26, 98, new Color(175, 145, 215, 190));

        // Botão "INICIAR JORNADA" — gradiente verde vibrante
        int bx = btnIniciarJornada.x;
        int by = btnIniciarJornada.y;
        int bw = btnIniciarJornada.width;
        int bh = btnIniciarJornada.height;

        // Estrelinhas cintilantes ao lado do botão Iniciar Jornada
        desenharBrilhoCintilante(g2, bx - 22, by + bh / 2, 9, new Color(255, 215, 75));
        desenharEstrela(g2, bx - 44, by + bh / 2 - 8, 10, 10);
        desenharBrilhoCintilante(g2, bx + bw + 22, by + bh / 2, 9, new Color(255, 215, 75));
        desenharEstrela(g2, bx + bw + 44, by + bh / 2 + 8, 10, -10);

        // Confetes mágicos salpicados nas laterais
        desenharConfetesMagicos(g2, cx + 50, by + bh / 2);
        desenharConfetesMagicos(g2, cx + CONTAINER_W - 65, by + bh / 2);

        // Animação de clique
        boolean isJornadaClicked = (clickRect == btnIniciarJornada && clickScale != 1.0f);
        float hAnim = hoverIniciarAnim;
        float currentScale = isJornadaClicked ? clickScale : (1.0f + 0.035f * hAnim);
        float liftY = isJornadaClicked ? 0 : (-4.0f * hAnim);

        AffineTransform btnOld = g2.getTransform();
        g2.translate(bx + bw / 2.0, by + bh / 2.0 + liftY);
        g2.scale(currentScale, currentScale);
        g2.translate(-(bx + bw / 2.0), -(by + bh / 2.0));

        // Sombra colorida com projecao dinamica no hover
        int shadowAlpha = Math.min(70, (int)(35 + 25 * hAnim));
        g2.setColor(new Color(45, 122, 38, shadowAlpha));
        g2.fillRoundRect(bx + 3, by + 6 + (int)(4 * hAnim), bw, bh, 22, 22);

        // Gradiente verde mais vibrante (iluminado no hover)
        Color topGreen = hAnim > 0 ? new Color(140, 225, 165) : new Color(120, 210, 150);
        Color botGreen = hAnim > 0 ? new Color(75, 185, 105) : new Color(60, 170, 90);
        GradientPaint grad = new GradientPaint(bx, by, topGreen, bx, by + bh, botGreen);
        g2.setPaint(grad);
        g2.fillRoundRect(bx, by, bw, bh, 22, 22);

        // Brilho no topo
        g2.setColor(new Color(255, 255, 255, (int)(55 + 30 * hAnim)));
        g2.fillRoundRect(bx + 4, by + 2, bw - 8, bh / 3, 18, 18);

        // Borda tracejada verde escuro sutil
        g2.setColor(new Color(40, 130, 70, 140));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8, 5}, 0));
        g2.drawRoundRect(bx + 2, by + 2, bw - 4, bh - 4, 18, 18);

        // Texto "INICIAR JORNADA"
        g2.setColor(Color.WHITE);
        g2.setFont(fontCabin.deriveFont(28f));
        FontMetrics fmB = g2.getFontMetrics();
        int textX = bx + (bw - fmB.stringWidth("INICIAR JORNADA")) / 2;
        int textY = by + (bh - fmB.getHeight()) / 2 + fmB.getAscent();
        g2.drawString("INICIAR JORNADA", textX, textY);

        // Efeito shimmer diagonal animado sobre o botão
        int shimmerPos = (int)(animTicks * 4.5) % (bw + 80) - 40;
        Shape oldClipBtn = g2.getClip();
        g2.setClip(new java.awt.geom.RoundRectangle2D.Float(bx, by, bw, bh, 22, 22));
        Composite oldCompBtn = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
        g2.setColor(Color.WHITE);
        int sx = bx + shimmerPos;
        g2.fillPolygon(new int[]{sx - 20, sx + 10, sx + 20, sx - 10}, new int[]{by, by, by + bh, by + bh}, 4);
        g2.setComposite(oldCompBtn);
        g2.setClip(oldClipBtn);

        if (btnOld != null) g2.setTransform(btnOld);

        // Cards de hobbies em grid 3 colunas proporcionais
        desenharCardHobby(g2, cardArte, "\ud83c\udfa8", "ARTE", "Desenho e pintura", 0);
        desenharCardHobby(g2, cardLeitura, "\ud83d\udcd6", "LEITURA", "Livros e mang\u00e1s", 1);
        desenharCardHobby(g2, cardFitness, "\ud83c\udfcb\ufe0f", "FITNESS", "Exerc\u00edcios di\u00e1rios", 2);

        // Enfeites artesanais abaixo dos cards (sem cobrir o conteúdo)
        int cardsBottom = cardArte.y + cardArte.height;
        desenharPlantinhaSuculenta(g2, cardArte.x + 2, cardsBottom + 3, 20);
        desenharMiniLapis(g2, cardArte.x + 30, cardsBottom + 16, -38);
        desenharXicaraFofa(g2, cardFitness.x + cardFitness.width - 32, cardsBottom + 3, 26, 22);
        desenharRaminhoFolhas(g2, cardFitness.x + cardFitness.width - 72, cardsBottom + 12, 18);
        desenharPetalaSolta(g2, cardArte.x + cardArte.width + 11, cardArte.y + cardArte.height / 2 - 10, 6, new Color(255, 195, 210), 25);
        desenharBrilhoCintilante(g2, cardLeitura.x + cardLeitura.width + 11, cardLeitura.y + cardLeitura.height / 2 - 10, 7, new Color(255, 215, 75));

        // ── Cartão "Dica do Dia" estilo bullet journal ──
        int maxNavY = h - 92;
        int noteW = Math.min(CONTAINER_W - 60, 720);
        int noteX = cx + (CONTAINER_W - noteW) / 2;

        String[] dicas = {
            "Reserve 15 minutos para seu hobby e veja sua criatividade florescer!",
            "Pequenos passos todos os dias constroem grandes conquistas.",
            "Cada rabisco revela um pouco mais da sua imaginação.",
            "Alongue-se, respire fundo e volte com a mente renovada.",
            "Leia algumas páginas hoje: toda aventura começa assim.",
            "N\u00e3o existe arte errada, s\u00f3 o seu jeito \u00fanico de ver o mundo."
        };
        int diaAno = java.time.LocalDate.now().getDayOfYear();
        String tipMsg = dicas[diaAno % dicas.length];

        Font tipFont = (fontCaveat != null ? fontCaveat : new Font("SansSerif", Font.ITALIC, 18))
                .deriveFont(Font.PLAIN, 18f);
        g2.setFont(tipFont);
        FontMetrics fmTip = g2.getFontMetrics();
        List<String> linhasTip = quebraTexto(tipMsg, fmTip, noteW - 96);
        int maxLinhas = 2;
        int noteH = 60 + Math.min(maxLinhas, Math.max(1, linhasTip.size())) * 20;

        // Centraliza o cartão no espaço livre entre os cards e a barra de navegação
        int navTop = h - 78;
        int noteY = cardsBottom + 26;
        int espacoLivre = (navTop - 14) - noteY - noteH;
        if (espacoLivre > 0) noteY += espacoLivre / 2;
        if (noteY + noteH > maxNavY) noteY = maxNavY - noteH;

        if (noteY >= cardsBottom + 14 && noteY + noteH <= maxNavY) {
            // Sombra suave
            g2.setColor(new Color(0, 0, 0, 16));
            g2.fillRoundRect(noteX + 3, noteY + 4, noteW, noteH, 18, 18);

            // Papel creme delicado com gradiente
            GradientPaint paperGrad = new GradientPaint(
                noteX, noteY, new Color(255, 254, 247),
                noteX, noteY + noteH, new Color(255, 250, 236));
            g2.setPaint(paperGrad);
            g2.fillRoundRect(noteX, noteY, noteW, noteH, 18, 18);

            // Faixa de destaque dourada na lateral esquerda
            Shape clipNote = g2.getClip();
            g2.setClip(new RoundRectangle2D.Float(noteX, noteY, noteW, noteH, 18, 18));
            g2.setColor(new Color(226, 183, 64, 200));
            g2.fillRect(noteX, noteY, 6, noteH);
            g2.setClip(clipNote);

            // Borda pontilhada costurada
            g2.setColor(new Color(220, 205, 180));
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{4, 4}, 0));
            g2.drawRoundRect(noteX + 5, noteY + 5, noteW - 10, noteH - 10, 14, 14);

            // Washi tapes prendendo o topo da anotação
            desenharWashiTapePoas(g2, noteX + 24, noteY - 7, 52, 14, new Color(255, 215, 140, 220), -5);
            desenharFitaScrapbook(g2, noteX + noteW - 74, noteY - 7, 50, 14, new Color(248, 175, 192, 220), 4);

            // Aspas decorativas
            g2.setColor(new Color(226, 183, 64, 110));
            g2.setFont(fontTitulo.deriveFont(38f));
            g2.drawString("\u201C", noteX + 20, noteY + 34);

            // Título da dica
            g2.setColor(new Color(90, 80, 65));
            g2.setFont(fontCaveat != null ? fontCaveat.deriveFont(Font.BOLD, 19f) : new Font("SansSerif", Font.BOLD, 18));
            g2.drawString("\u2728 Dica de Hoje", noteX + 48, noteY + 28);

            // Mensagem inspiradora (quebra automática, até 2 linhas)
            g2.setColor(new Color(110, 100, 85));
            g2.setFont(tipFont);
            int ly = noteY + 50;
            for (int i = 0; i < linhasTip.size() && i < maxLinhas; i++) {
                String linha = linhasTip.get(i);
                if (i == maxLinhas - 1 && linhasTip.size() > maxLinhas) linha = linha + "...";
                g2.drawString(linha, noteX + 48, ly);
                ly += 20;
            }

            // Enfeites no canto direito do cartão
            desenharBrilhoCintilante(g2, noteX + noteW - 28, noteY + 24, 8, new Color(255, 215, 75));
            desenharEstrela(g2, noteX + noteW - 52, noteY + 20, 7, animTicks * 1.1 % 360);
            desenharCoracaoRabisco(g2, noteX + noteW - 78, noteY + noteH - 22, 10, new Color(245, 150, 170, 180));
        }
    }

    private void desenharCardHobby(Graphics2D g2, Rectangle r, String icone, String titulo, String desc, int idx) {
        int x = r.x, y = r.y, w = r.width, h = r.height;
        boolean hover = (hoverCard == idx);

        // Animação de clique no card
        Rectangle cardRef = (idx == 0) ? cardArte : (idx == 1) ? cardLeitura : cardFitness;
        boolean isClicked = (clickRect == cardRef && clickScale != 1.0f);
        AffineTransform cardOld = null;
        if (isClicked || hover) {
            cardOld = g2.getTransform();
            float s = isClicked ? clickScale : 1.04f;
            int hoverOffY = hover && !isClicked ? -4 : 0;
            g2.translate(x + w / 2.0, y + h / 2.0 + hoverOffY);
            g2.scale(s, s);
            g2.translate(-(x + w / 2.0), -(y + h / 2.0 + hoverOffY));
        }

        // Cores únicas por hobby
        Color corFundo1, corFundo2, corBorda, corTitulo, corAccent;
        switch (idx) {
            case 0 -> { // ARTE — rosa/coral
                corFundo1 = new Color(255, 230, 240);
                corFundo2 = new Color(252, 205, 220);
                corBorda = new Color(230, 150, 175);
                corTitulo = new Color(200, 80, 120);
                corAccent = new Color(241, 160, 196);
            }
            case 1 -> { // LEITURA — azul/turquesa
                corFundo1 = new Color(220, 240, 255);
                corFundo2 = new Color(190, 225, 250);
                corBorda = new Color(130, 180, 220);
                corTitulo = new Color(50, 120, 170);
                corAccent = new Color(100, 170, 210);
            }
            default -> { // FITNESS — verde limão
                corFundo1 = new Color(230, 255, 220);
                corFundo2 = new Color(200, 240, 190);
                corBorda = new Color(140, 200, 100);
                corTitulo = new Color(70, 140, 50);
                corAccent = new Color(168, 214, 72);
            }
        }

        // Sombra suave (mais forte no hover)
        g2.setColor(new Color(0, 0, 0, hover ? 32 : 16));
        g2.fillRoundRect(x + 3, y + (hover ? 8 : 5), w, h, 22, 22);

        // Fundo com gradiente colorido
        GradientPaint cardGrad = new GradientPaint(x, y, corFundo1, x, y + h, corFundo2);
        g2.setPaint(cardGrad);
        g2.fillRoundRect(x, y, w, h, 22, 22);

        // Faixa de cor no topo do card
        Shape clipOld = g2.getClip();
        g2.setClip(new java.awt.geom.RoundRectangle2D.Float(x, y, w, h, 22, 22));
        g2.setColor(corAccent);
        g2.fillRect(x, y, w, 6);
        g2.setClip(clipOld);

        // Borda colorida
        g2.setColor(hover ? corTitulo : corBorda);
        g2.setStroke(new BasicStroke(hover ? 2.5f : 1.5f));
        g2.drawRoundRect(x, y, w, h, 22, 22);

        // Costura interna estilo scrapbook / caderninho
        g2.setColor(new Color(corBorda.getRed(), corBorda.getGreen(), corBorda.getBlue(), hover ? 160 : 100));
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{4, 4}, 0));
        g2.drawRoundRect(x + 5, y + 5, w - 10, h - 10, 16, 16);

        // Fita washi tape segurando o topo do card no caderno
        if (idx == 0) {
            desenharWashiTapePoas(g2, x + 18, y - 8, 56, 16, new Color(255, 175, 200, 225), -6);
        } else if (idx == 1) {
            desenharFitaScrapbook(g2, x + w / 2 - 28, y - 8, 56, 16, new Color(135, 195, 240), 2);
        } else {
            desenharWashiTapePoas(g2, x + w - 74, y - 8, 56, 16, new Color(160, 220, 140, 225), 6);
        }

        // Estrelinha brilhante no canto superior do card
        desenharEstrela(g2, x + w - 20, y + 20, 8, idx == 0 ? 12 : idx == 1 ? -10 : 15);

        // Efeito hover — brilho mais evidente
        if (hover) {
            g2.setColor(new Color(255, 255, 255, 50));
            g2.fillRoundRect(x, y, w, h, 22, 22);
        }

        // ── Ícone em selo circular ──
        int badgeR = Math.min(30, Math.max(24, w / 9));
        int badgeCX = x + w / 2;
        int badgeCY = y + 54;
        g2.setColor(new Color(255, 255, 255, 110));
        g2.fillOval(badgeCX - badgeR - 4, badgeCY - badgeR - 4, (badgeR + 4) * 2, (badgeR + 4) * 2);
        g2.setColor(new Color(255, 255, 255, 225));
        g2.fillOval(badgeCX - badgeR, badgeCY - badgeR, badgeR * 2, badgeR * 2);
        g2.setColor(corBorda);
        g2.setStroke(new BasicStroke(1.6f));
        g2.drawOval(badgeCX - badgeR, badgeCY - badgeR, badgeR * 2, badgeR * 2);

        int iconSize = Math.min(34, Math.max(26, badgeR));
        g2.setFont(new Font("SansSerif", Font.PLAIN, iconSize));
        FontMetrics fmI = g2.getFontMetrics();
        int iconW = fmI.stringWidth(icone);
        int iconBaseline = badgeCY + (fmI.getAscent() - fmI.getDescent()) / 2;
        g2.drawString(icone, badgeCX - iconW / 2, iconBaseline);

        // ── Título com cor do hobby ──
        g2.setColor(corTitulo);
        float titleSize = Math.min(23f, Math.max(17f, w * 0.085f));
        g2.setFont(fontCabin.deriveFont(Font.BOLD, titleSize));
        FontMetrics fmT = g2.getFontMetrics();
        g2.drawString(titulo, x + (w - fmT.stringWidth(titulo)) / 2, y + 108);

        // ── Descrição ──
        g2.setFont(fontCabin.deriveFont(13f));
        g2.setColor(new Color(90, 82, 62));
        FontMetrics fmD = g2.getFontMetrics();
        g2.drawString(desc, x + (w - fmD.stringWidth(desc)) / 2, y + 130);

        // ── Progresso do hobby (nível + XP) ──
        int nv = (idx == 0) ? nivelArte : (idx == 1) ? nivelLeitura : nivelFitness;
        int xp = (idx == 0) ? xpArte : (idx == 1) ? xpLeitura : xpFitness;
        float progresso = Math.min(1f, (xp % 100) / 100f);

        int barX = x + 20;
        int barW = w - 40;
        int barY = y + h - 30;
        int barH = 9;

        g2.setFont(fontCabin.deriveFont(Font.BOLD, 12f));
        g2.setColor(corTitulo);
        g2.drawString("Nível " + nv, barX, barY - 9);
        String xpTxt = xp + " XP";
        FontMetrics fmX = g2.getFontMetrics();
        g2.setColor(new Color(110, 100, 80));
        g2.drawString(xpTxt, barX + barW - fmX.stringWidth(xpTxt), barY - 9);

        // Trilha da barra
        g2.setColor(new Color(255, 255, 255, 190));
        g2.fillRoundRect(barX, barY, barW, barH, barH, barH);
        g2.setColor(new Color(corBorda.getRed(), corBorda.getGreen(), corBorda.getBlue(), 120));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(barX, barY, barW, barH, barH, barH);

        // Preenchimento com gradiente do hobby
        int fillW = Math.max(0, (int)(barW * progresso));
        if (fillW > 0) {
            Shape oldClipBar = g2.getClip();
            g2.setClip(new RoundRectangle2D.Float(barX, barY, barW, barH, barH, barH));
            GradientPaint fillGrad = new GradientPaint(barX, barY, corAccent, barX + barW, barY, corTitulo);
            g2.setPaint(fillGrad);
            g2.fillRoundRect(barX, barY, fillW, barH, barH, barH);
            g2.setColor(new Color(255, 255, 255, 90));
            g2.fillRoundRect(barX + 1, barY + 1, Math.max(0, fillW - 2), barH / 2, barH, barH);
            g2.setClip(oldClipBar);
        }

        if (cardOld != null) g2.setTransform(cardOld);
    }

    // ==========================================
    // 3. TELA DESEMPENHO
    // ==========================================
    private void desenharDesempenho(Graphics2D g2, int cx, int w, int h) {
        g2.setStroke(new BasicStroke(1.5f));
        int centerW = CONTAINER_W - 40;
        int centerX = cx + 20;

        // ── Título estilo Perfil / Ajuda ──
        g2.setFont(fontTitulo.deriveFont(38f));
        String titulo = "DESEMPENHO";
        FontMetrics fmTit = g2.getFontMetrics();
        int titW = fmTit.stringWidth(titulo);
        int titX = cx + (CONTAINER_W - titW) / 2;
        int titY = 46;

        // Washi tape decorativa atrás do título (mais sutil e abaixo das letras)
        desenharWashiTapePoas(g2, titX + titW / 2 - 60, titY - 24, 120, 16, new Color(255, 235, 170, 150), 0);

        // Outline branco e texto tracejado/duplo
        g2.setColor(Color.WHITE);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx != 0 || dy != 0) g2.drawString(titulo, titX + dx, titY + dy);
            }
        }
        g2.setColor(new Color(110, 140, 195));
        g2.drawString(titulo, titX, titY);

        // Estrelinhas e sparkles ao lado do título
        desenharBrilhoCintilante(g2, titX - 35, titY - 12, 10, new Color(255, 215, 75));
        desenharEstrela(g2, titX - 60, titY - 8, 9, animTicks * 1.3 % 360);
        desenharBrilhoCintilante(g2, titX + titW + 35, titY - 12, 10, new Color(255, 215, 75));
        desenharEstrela(g2, titX + titW + 60, titY - 8, 9, -(animTicks * 1.3 % 360));

        // ── Banner guia creme estilo Ajuda ──
        int banY = titY + 14;
        int banH = 34;
        g2.setColor(new Color(255, 250, 225, 220));
        g2.fillRoundRect(centerX + 40, banY, centerW - 80, banH, 18, 18);
        g2.setColor(new Color(230, 205, 130, 180));
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5, 4}, 0));
        g2.drawRoundRect(centerX + 40, banY, centerW - 80, banH, 18, 18);
        g2.setFont(fontCabin.deriveFont(Font.BOLD, 13f));
        g2.setColor(new Color(150, 115, 60));
        String bannerTxt = "Acompanhe sua evolu\u00e7\u00e3o di\u00e1ria e conquiste novos recordes!";
        FontMetrics fmBan = g2.getFontMetrics();
        g2.drawString(bannerTxt, centerX + (centerW - fmBan.stringWidth(bannerTxt)) / 2, banY + 22);
        g2.setStroke(new BasicStroke(1.5f));

        // ── 3 mini cards de stat individuais estilo Scrapbook ──
        int statY = banY + banH + 16;
        int statH = 96;
        int statGap = 16;
        int statW = (centerW - statGap * 2) / 3;

        String[] statVal   = { "" + totalXP, "" + totalDesafios, "" + streakDias };
        String[] statLbl   = { "TOTAL XP", "DESAFIOS FEITOS", "DIAS SEGUIDOS" };
        String[] statTags  = { "Experi\u00eancia", "Miss\u00f5es", "Ofensiva" };
        Color[] statCor    = { new Color(235, 110, 85), new Color(75, 150, 215), new Color(85, 175, 110) };
        Color[] statBg     = { new Color(255, 245, 242), new Color(242, 248, 255), new Color(242, 253, 245) };
        Color[] statBorder = { new Color(245, 195, 180), new Color(185, 215, 245), new Color(185, 230, 195) };

        for (int i = 0; i < 3; i++) {
            int sX = centerX + i * (statW + statGap);

            // Sombra suave
            g2.setColor(new Color(0, 0, 0, 12));
            g2.fillRoundRect(sX + 3, statY + 4, statW, statH, 18, 18);

            // Fundo
            GradientPaint sg = new GradientPaint(sX, statY, statBg[i], sX, statY + statH, Color.WHITE);
            g2.setPaint(sg);
            g2.fillRoundRect(sX, statY, statW, statH, 18, 18);

            // Borda
            g2.setColor(statBorder[i]);
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawRoundRect(sX, statY, statW, statH, 18, 18);

            // Costura pontilhada interna
            g2.setColor(new Color(statBorder[i].getRed(), statBorder[i].getGreen(), statBorder[i].getBlue(), 120));
            g2.setStroke(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{4, 4}, 0));
            g2.drawRoundRect(sX + 5, statY + 5, statW - 10, statH - 10, 14, 14);
            g2.setStroke(new BasicStroke(1.5f));

            // Washi tape no topo
            desenharWashiTapePoas(g2, sX + statW / 2 - 32, statY - 9, 64, 14,
                new Color(statBorder[i].getRed(), statBorder[i].getGreen(), statBorder[i].getBlue(), 200), 0);

            // Tag superior fofa
            g2.setFont(fontCabin.deriveFont(Font.BOLD, 10f));
            g2.setColor(statCor[i].darker());
            FontMetrics fmTag = g2.getFontMetrics();
            g2.drawString(statTags[i].toUpperCase(), sX + (statW - fmTag.stringWidth(statTags[i].toUpperCase())) / 2, statY + 22);

            // Valor numérico grande e destacado
            g2.setFont(fontCaveat.deriveFont(Font.BOLD, 42f));
            FontMetrics fmV = g2.getFontMetrics();
            int vX = sX + (statW - fmV.stringWidth(statVal[i])) / 2;
            g2.setColor(new Color(statCor[i].getRed(), statCor[i].getGreen(), statCor[i].getBlue(), 40));
            g2.drawString(statVal[i], vX + 2, statY + 62);
            g2.setColor(statCor[i]);
            g2.drawString(statVal[i], vX, statY + 60);

            // Label inferior
            g2.setFont(fontCabin.deriveFont(Font.BOLD, 11f));
            g2.setColor(new Color(110, 110, 115));
            FontMetrics fmL = g2.getFontMetrics();
            g2.drawString(statLbl[i], sX + (statW - fmL.stringWidth(statLbl[i])) / 2, statY + 84);

            // Pequeno brilho decorativo no canto
            desenharBrilhoCintilante(g2, sX + statW - 16, statY + 16, 5, statCor[i]);
        }

        // ── Card Meus Hobbies ──
        int hY = statY + statH + 18;
        int hH = h - hY - 68;
        if (hH < 220) hH = 220;

        // Sombra
        g2.setColor(new Color(0, 0, 0, 10));
        g2.fillRoundRect(centerX + 3, hY + 4, centerW, hH, 22, 22);

        // Fundo gradiente azul/creme scrapbook
        GradientPaint hobbiesGrad = new GradientPaint(centerX, hY, new Color(248, 251, 255), centerX, hY + hH, new Color(236, 244, 255));
        g2.setPaint(hobbiesGrad);
        g2.fillRoundRect(centerX, hY, centerW, hH, 22, 22);

        // Borda tracejada azul suave
        g2.setColor(new Color(175, 200, 230));
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6, 4}, 0));
        g2.drawRoundRect(centerX, hY, centerW, hH, 22, 22);

        // Costura pontilhada interna
        g2.setColor(new Color(175, 200, 230, 90));
        g2.setStroke(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{4, 4}, 0));
        g2.drawRoundRect(centerX + 6, hY + 6, centerW - 12, hH - 12, 16, 16);
        g2.setStroke(new BasicStroke(1.5f));

        // Washi tape central no topo do card
        desenharFitaScrapbook(g2, centerX + (centerW - 120) / 2, hY - 11, 120, 17, new Color(168, 216, 234, 210), 0);

        // Título "MEUS HOBBIES"
        g2.setFont(fontCabin.deriveFont(Font.BOLD, 18f));
        g2.setColor(new Color(85, 125, 180));
        FontMetrics fmH2 = g2.getFontMetrics();
        String hobbiesTit = "PROGRESSO DOS HOBBIES";
        g2.drawString(hobbiesTit, centerX + (centerW - fmH2.stringWidth(hobbiesTit)) / 2, hY + 34);
        desenharBrilhoCintilante(g2, centerX + (centerW - fmH2.stringWidth(hobbiesTit)) / 2 - 22, hY + 28, 6, new Color(130, 175, 235));
        desenharBrilhoCintilante(g2, centerX + (centerW + fmH2.stringWidth(hobbiesTit)) / 2 + 22, hY + 28, 6, new Color(130, 175, 235));

        // Divisória pontilhada charmosa
        g2.setColor(new Color(195, 215, 240, 160));
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5, 5}, 0));
        g2.drawLine(centerX + 35, hY + 44, centerX + centerW - 35, hY + 44);
        g2.setStroke(new BasicStroke(1.5f));

        // Layout vertical distribuído para as 3 seções de hobbies
        int availableHeight = hH - 60;
        int barSlotH = availableHeight / 3;
        int barAreaW = centerW - 60;
        int barStartX = centerX + 30;

        desenharBarraHobbyRica(g2, barStartX, hY + 54, barAreaW, "LEITURA", "N\u00edvel " + nivelLeitura, xpLeitura % 100, 100, new Color(90, 165, 215), new Color(230, 245, 255), "Meta ativa: Leia 10 páginas por dia", "\uD83D\uDCD6 15 XP acumulados");
        desenharBarraHobbyRica(g2, barStartX, hY + 54 + barSlotH, barAreaW, "ARTE", "N\u00edvel " + nivelArte, xpArte % 100, 100, COLOR_PILL_ARTE, new Color(255, 240, 246), "Meta ativa: Pratique desenho por 20 minutos", "\uD83C\uDFA8 10 XP acumulados");
        desenharBarraHobbyRica(g2, barStartX, hY + 54 + barSlotH * 2, barAreaW, "FITNESS", "N\u00edvel " + nivelFitness, xpFitness % 100, 100, new Color(135, 195, 60), new Color(245, 253, 235), "Meta ativa: Caminhada ou treino do dia", "\uD83C\uDFC3 0 XP acumulados");
    }

    /** Coluna de stat enriquecida com mini ícone e glow pulsante */
    private void desenharStatColunaRica(Graphics2D g2, int x, int y, int w, String valor, String label, Color cor, String icone) {
        // Mini ícone acima do número
        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        FontMetrics fmI = g2.getFontMetrics();
        g2.drawString(icone, x + (w - fmI.stringWidth(icone)) / 2, y - 8);

        // Número grande com glow suave
        g2.setFont(fontCaveat.deriveFont(Font.BOLD, 38f));
        FontMetrics fmV = g2.getFontMetrics();
        int numX = x + (w - fmV.stringWidth(valor)) / 2;
        // Shadow
        g2.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), 50));
        g2.drawString(valor, numX + 2, y + 2);
        g2.setColor(cor);
        g2.drawString(valor, numX, y);

        // Label abaixo
        g2.setFont(fontCaveat.deriveFont(Font.ITALIC, 15f));
        g2.setColor(new Color(90, 90, 90));
        FontMetrics fmL = g2.getFontMetrics();
        g2.drawString(label, x + (w - fmL.stringWidth(label)) / 2, y + 22);

        // Divisória vertical entre colunas (exceto última)
        g2.setColor(new Color(210, 195, 150, 100));
        g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{3, 3}, 0));
        g2.drawLine(x + w - 1, y - 30, x + w - 1, y + 28);
    }

    /** Barra de hobby enriquecida com gradiente, cápsula, meta detalhada e shimmer */
    private void desenharBarraHobbyRica(Graphics2D g2, int x, int y, int w, String nome, String nivel, int atual, int max, Color fillCor, Color pillBg, String dicaOuMeta, String subStatus) {
        // Tag/Pill fofa com o nome da categoria à esquerda
        g2.setFont(fontCabin.deriveFont(Font.BOLD, 13f));
        FontMetrics fmN = g2.getFontMetrics();
        int pillW = fmN.stringWidth(nome) + 24;
        int pillH = 22;

        // Fundo da pill do nome
        g2.setColor(pillBg);
        g2.fillRoundRect(x, y - 2, pillW, pillH, 12, 12);
        g2.setColor(fillCor);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(x, y - 2, pillW, pillH, 12, 12);
        g2.drawString(nome, x + 12, y + 14);

        // Badge de nível à direita
        int nv = Integer.parseInt(nivel.replaceAll("[^0-9]", ""));
        String nivelTxt = "NÍVEL " + nv;
        FontMetrics fmNv = g2.getFontMetrics();
        int nvW = fmNv.stringWidth(nivelTxt) + 20;
        int nvX = x + w - nvW;

        g2.setColor(fillCor);
        g2.fillRoundRect(nvX, y - 2, nvW, pillH, 12, 12);
        g2.setColor(Color.WHITE);
        g2.drawString(nivelTxt, nvX + 10, y + 14);

        // XP atual/max entre o nome e o badge
        String xpTxt = atual + " / " + max + " XP";
        g2.setFont(fontCabin.deriveFont(Font.BOLD, 12f));
        g2.setColor(new Color(110, 115, 125));
        FontMetrics fmXp = g2.getFontMetrics();
        g2.drawString(xpTxt, nvX - fmXp.stringWidth(xpTxt) - 14, y + 14);

        // Barra de progresso com design arredondado e borda fofa
        int barY = y + pillH + 5;
        int barH = 16;

        // Fundo da barra
        g2.setColor(new Color(230, 235, 242));
        g2.fillRoundRect(x, barY, w, barH, barH, barH);
        g2.setColor(new Color(205, 215, 225));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(x, barY, w, barH, barH, barH);

        // Preenchimento com gradiente de cor suave
        int fw = (int) (w * Math.max(0.04, Math.min(1.0, (double) atual / max)));
        Color fillTop = new Color(
            Math.min(255, fillCor.getRed() + 35),
            Math.min(255, fillCor.getGreen() + 35),
            Math.min(255, fillCor.getBlue() + 35)
        );
        GradientPaint barGrad = new GradientPaint(x, barY, fillTop, x, barY + barH, fillCor);
        g2.setPaint(barGrad);
        g2.fillRoundRect(x, barY, fw, barH, barH, barH);

        // Bordinha sutil no preenchimento
        g2.setColor(new Color(fillCor.getRed(), fillCor.getGreen(), fillCor.getBlue(), 120));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(x, barY, fw, barH, barH, barH);

        // Brilho na metade superior da barra preenchida
        g2.setColor(new Color(255, 255, 255, 80));
        g2.fillRoundRect(x + 2, barY + 1, Math.max(0, fw - 4), barH / 2 - 1, barH / 2, barH / 2);

        // Shimmer animado na barra preenchida
        if (fw > 20) {
            int shimPos = (int)(animTicks * 3.5) % (fw + 30) - 15;
            Shape oldClip = g2.getClip();
            g2.setClip(new java.awt.geom.RoundRectangle2D.Float(x, barY, fw, barH, barH, barH));
            Composite oldComp = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
            g2.setColor(Color.WHITE);
            g2.fillRect(x + shimPos, barY, 14, barH);
            g2.setComposite(oldComp);
            g2.setClip(oldClip);
        }

        // Mini estrela/brilho no final da barra
        if (fw > 10) {
            desenharBrilhoCintilante(g2, x + fw, barY + barH / 2, 4, Color.WHITE);
        }

        // ── DETALHES EXTRAS PARA PREENCHER O ESPAÇO ENTRE AS LINHAS ──
        int detY = barY + barH + 15;

        // Marcador/ícone de check ou estrela fofo
        desenharEstrela(g2, x + 5, detY - 4, 5, 0);

        // Texto da meta / atividade recomendada
        g2.setFont(fontCabin.deriveFont(Font.ITALIC, 12f));
        g2.setColor(new Color(95, 100, 110));
        g2.drawString(dicaOuMeta, x + 16, detY);

        // Status secundário à direita (ex: XP acumulado, taxa de conclusão)
        g2.setFont(fontCabin.deriveFont(Font.BOLD, 11f));
        g2.setColor(fillCor.darker());
        FontMetrics fmSub = g2.getFontMetrics();
        g2.drawString(subStatus, x + w - fmSub.stringWidth(subStatus), detY);

        // Linha pontilhada delicada de separação no final do slot
        g2.setColor(new Color(215, 225, 238, 120));
        g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{3, 4}, 0));
        g2.drawLine(x, detY + 10, x + w, detY + 10);
        g2.setStroke(new BasicStroke(1.5f));
    }

    private void desenharWashiTape(Graphics2D g2, int x, int y, int w, int h, Color cor) {
        g2.setColor(cor);
        g2.fillRect(x, y, w, h);
        // Textura sutil de listras
        g2.setColor(new Color(255, 255, 255, 30));
        g2.setStroke(new BasicStroke(0.5f));
        for (int i = 0; i < w; i += 6) {
            g2.drawLine(x + i, y, x + i + 3, y + h);
        }
    }

    private void desenharStatColuna(Graphics2D g2, int x, int y, int w, String valor, String label) {
        // Número grande em coral/salmão
        g2.setFont(fontCaveat.deriveFont(Font.BOLD, 36f));
        g2.setColor(COLOR_CORAL);
        FontMetrics fmV = g2.getFontMetrics();
        g2.drawString(valor, x + (w - fmV.stringWidth(valor)) / 2, y);

        // Label abaixo
        g2.setFont(fontCaveat.deriveFont(Font.ITALIC, 16f));
        g2.setColor(new Color(90, 90, 90));
        FontMetrics fmL = g2.getFontMetrics();
        g2.drawString(label, x + (w - fmL.stringWidth(label)) / 2, y + 22);
    }

    private void desenharBarraHobby(Graphics2D g2, int x, int y, int w, String nome, String nivel, int atual, int max, Color fillCor) {
        // Nome do hobby
        g2.setFont(fontCabin.deriveFont(16f));
        g2.setColor(new Color(40, 40, 40));
        g2.drawString(nome, x, y + 14);

        // Nível à direita em itálico
        g2.setFont(fontCaveat.deriveFont(Font.ITALIC, 16f));
        g2.setColor(new Color(110, 110, 110));
        FontMetrics fmN = g2.getFontMetrics();
        g2.drawString(nivel, x + w - fmN.stringWidth(nivel), y + 14);

        // Barra de fundo cinza
        int barH = 14;
        g2.setColor(new Color(228, 228, 228));
        g2.fillRoundRect(x, y + 22, w, barH, barH, barH);

        // Preenchimento colorido
        int fw = (int) (w * Math.max(0.08, Math.min(1.0, (double) atual / max)));
        g2.setColor(fillCor);
        g2.fillRoundRect(x, y + 22, fw, barH, barH, barH);

        // Brilho na barra
        g2.setColor(new Color(255, 255, 255, 50));
        g2.fillRoundRect(x + 2, y + 23, fw - 4, barH / 2, barH / 2, barH / 2);
    }

    // ==========================================
    // 4. TELA OBJETIVOS
    // ==========================================
    private void desenharObjetivos(Graphics2D g2, int cx, int w, int h) {
        // ── Título estilo Perfil / Scrapbook ──
        g2.setFont(fontTitulo.deriveFont(38f));
        String titulo = "MEUS OBJETIVOS";
        FontMetrics fmTit = g2.getFontMetrics();
        int titW = fmTit.stringWidth(titulo);
        int titX = cx + (CONTAINER_W - titW) / 2;
        int titY = 46;

        // Fita washi tape decorativa atrás do título
        desenharWashiTapePoas(g2, titX + titW / 2 - 60, titY - 24, 120, 16, new Color(255, 235, 170, 150), 0);

        // Outline branco e texto coral/scrapbook
        g2.setColor(Color.WHITE);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx != 0 || dy != 0) g2.drawString(titulo, titX + dx, titY + dy);
            }
        }
        g2.setColor(new Color(235, 115, 95));
        g2.drawString(titulo, titX, titY);

        // Estrelinhas e sparkles ao lado do título (igual Perfil)
        desenharBrilhoCintilante(g2, titX - 35, titY - 12, 10, new Color(255, 215, 75));
        desenharEstrela(g2, titX - 60, titY - 8, 9, animTicks * 1.1 % 360);
        desenharBrilhoCintilante(g2, titX + titW + 35, titY - 12, 10, new Color(255, 215, 75));
        desenharEstrela(g2, titX + titW + 60, titY - 8, 9, -(animTicks * 1.1 % 360));

        // ── Banner guia creme estilo Perfil / Ajuda ──
        int centerW = CONTAINER_W - 40;
        int centerX = cx + 20;
        int banY = titY + 14;
        int banH = 34;

        g2.setColor(new Color(255, 250, 225, 220));
        g2.fillRoundRect(centerX + 40, banY, centerW - 80, banH, 18, 18);
        g2.setColor(new Color(230, 205, 130, 180));
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5, 4}, 0));
        g2.drawRoundRect(centerX + 40, banY, centerW - 80, banH, 18, 18);
        g2.setFont(fontCabin.deriveFont(Font.BOLD, 13f));
        g2.setColor(new Color(150, 115, 60));
        String bannerTxt = "Marque suas metas di\u00e1rias para ganhar XP e subir de n\u00edvel!";
        FontMetrics fmBan = g2.getFontMetrics();
        g2.drawString(bannerTxt, centerX + (centerW - fmBan.stringWidth(bannerTxt)) / 2, banY + 22);
        g2.setStroke(new BasicStroke(1.5f));

        // Ponto inicial vertical com desconto de scroll
        int startY = banY + banH + 16 - scrollY;

        // Espaçamento entre os blocos de hobby
        int gapSecoes = 18;

        // Seção Leitura 📖
        int yFimLeitura = desenharSecaoObjetivo(g2, cx, startY, "Leitura", COLOR_PILL_LEITURA,
                metasLeitura, txtNovaMetaLeitura, btnAddLeitura, btnRefLeitura);

        // Seção Arte 🎨
        int yFimArte = desenharSecaoObjetivo(g2, cx, yFimLeitura + gapSecoes, "Arte", COLOR_PILL_ARTE,
                metasArte, txtNovaMetaArte, btnAddArte, btnRefArte);

        // Seção Fitness 🏋️
        int yFimFitness = desenharSecaoObjetivo(g2, cx, yFimArte + gapSecoes, "Fitness", COLOR_PILL_FITNESS,
                metasFitness, txtNovaMetaFitness, btnAddFitness, btnRefFitness);

        // Cálculo dinâmico do limite de rolagem com margem inferior ampla
        int conteudoTotal = (yFimFitness + scrollY) - (banY + banH + 16);
        int areaVisivel = h - (banY + banH + 16) - 68;
        maxScrollObjetivos = Math.max(0, conteudoTotal - areaVisivel + 30);

        // Barra de rolagem discreta e elegante quando o conteúdo excede o espaço
        if (maxScrollObjetivos > 0) {
            int barX = cx + CONTAINER_W - 8;
            int barY = banY + banH + 16;
            int barH = areaVisivel;
            g2.setColor(new Color(0, 0, 0, 15));
            g2.fillRoundRect(barX, barY, 5, barH, 4, 4);

            int thumbH = Math.max(26, (int) ((double) areaVisivel / (conteudoTotal + 15) * barH));
            int thumbY = barY + (int) ((double) scrollY / maxScrollObjetivos * (barH - thumbH));
            g2.setColor(new Color(235, 115, 95, 160));
            g2.fillRoundRect(barX, thumbY, 5, thumbH, 4, 4);
        }

        // Renderizar animações flutuantes de XP (+XP) que aparecem e desaparecem
        desenharAnimacoesXP(g2);
    }

    private void desenharAnimacoesXP(Graphics2D g2) {
        if (animacoesXP.isEmpty()) return;

        List<AnimacaoXP> ativas = new ArrayList<>();
        Composite oldComp = g2.getComposite();

        for (AnimacaoXP anim : animacoesXP) {
            if (anim.estaAtiva()) {
                ativas.add(anim);
                float p = anim.getProgresso();

                // Flutua suavemente para cima (cerca de 26 pixels)
                int curY = anim.y - (int)(p * 26f);
                int curX = anim.x;

                // Opacidade calculada: surge suavemente (fade-in), se mantém nítido e depois desvanece (fade-out)
                float alpha = 1.0f;
                if (p < 0.15f) {
                    alpha = p / 0.15f;
                } else if (p > 0.60f) {
                    alpha = Math.max(0.0f, 1.0f - (p - 0.60f) / 0.40f);
                }

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1.0f, Math.max(0.0f, alpha))));

                // Pílula roxa oficial idêntica ao design do aplicativo
                int badgeW = 60;
                int badgeH = 22;

                // Sombra suave da badge
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fillRoundRect(curX + 1, curY + 2, badgeW, badgeH, 12, 12);

                // Fundo roxo da badge
                g2.setColor(COLOR_PURPLE_BADGE);
                g2.fillRoundRect(curX, curY, badgeW, badgeH, 12, 12);

                // Texto branco "+XX XP"
                g2.setColor(Color.WHITE);
                g2.setFont(fontCabin.deriveFont(Font.BOLD, 12f));
                FontMetrics fm = g2.getFontMetrics();
                int tx = curX + (badgeW - fm.stringWidth(anim.texto)) / 2;
                int ty = curY + 15;
                g2.drawString(anim.texto, tx, ty);
            }
        }

        g2.setComposite(oldComp);
        animacoesXP = ativas;
    }

    private int desenharSecaoObjetivo(Graphics2D g2, int cx, int y, String tituloSecao, Color pillColor,
            List<MetaItem> itens, JTextField txtInput, Rectangle btnPlus, Rectangle btnRef) {
        int cardW = Math.min(CONTAINER_W - 40, 1100);
        int cardX = cx + (CONTAINER_W - cardW) / 2;

        // Altura dinâmica compacta e harmônica
        int headerAreaH = 50;
        int itemH = 44;
        int itemGap = 6;
        int totalItensH = itens.size() * (itemH + itemGap);
        int inputH = 38;
        int cardH = headerAreaH + totalItensH + inputH + 14;

        // Sombra suave do card
        g2.setColor(new Color(0, 0, 0, 10));
        g2.fillRoundRect(cardX + 2, y + 3, cardW, cardH, 18, 18);

        // Fundo sutil do card (tom creme de caderno)
        g2.setColor(new Color(254, 250, 232));
        g2.fillRoundRect(cardX, y, cardW, cardH, 18, 18);

        // Borda suave do card para melhor separação visual
        g2.setColor(new Color(225, 215, 195, 140));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(cardX, y, cardW, cardH, 18, 18);

        // Costura interna estilo bullet journal
        g2.setColor(new Color(215, 205, 180, 100));
        g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{4, 4}, 0));
        g2.drawRoundRect(cardX + 4, y + 4, cardW - 8, cardH - 8, 14, 14);

        // Washi tape prendendo o topo esquerdo do card
        Color fitaCor = new Color(pillColor.getRed(), pillColor.getGreen(), pillColor.getBlue(), 180);
        desenharWashiTapePoas(g2, cardX + 20, y - 6, 52, 13, fitaCor, -3);

        // Pílula superior da categoria — proporcional
        int pillW = Math.min(cardW - 60, 480);
        int pillH = 38;
        int pillX = cardX + (cardW - pillW) / 2;

        g2.setColor(new Color(0, 0, 0, 10));
        g2.fillRoundRect(pillX + 2, y + 10, pillW, pillH, 18, 18);

        g2.setColor(pillColor);
        g2.fillRoundRect(pillX, y + 8, pillW, pillH, 18, 18);

        // Brilho suave no topo da pílula
        g2.setColor(new Color(255, 255, 255, 60));
        g2.fillRoundRect(pillX + 3, y + 9, pillW - 6, pillH / 3, 14, 14);

        // Texto branco na pílula
        g2.setColor(Color.WHITE);
        g2.setFont(fontCabin.deriveFont(Font.BOLD, 18f));
        FontMetrics fmP = g2.getFontMetrics();
        g2.drawString(tituloSecao, pillX + (pillW - fmP.stringWidth(tituloSecao)) / 2, y + 33);

        // Renderizar cada meta da lista — largura proporcional
        int curY = y + headerAreaH;
        int taskW = cardW - 40;
        int taskX = cardX + 20;

        for (int i = 0; i < itens.size(); i++) {
            MetaItem item = itens.get(i);

            // Sombra suave do item
            g2.setColor(new Color(0, 0, 0, 8));
            g2.fillRoundRect(taskX + 2, curY + 2, taskW, itemH, 12, 12);

            // Fundo do item
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(taskX, curY, taskW, itemH, 12, 12);

            // Borda do item
            g2.setColor(new Color(220, 220, 220));
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawRoundRect(taskX, curY, taskW, itemH, 12, 12);

            // Checkbox
            int checkSize = 22;
            int checkX = taskX + 12;
            int checkY = curY + (itemH - checkSize) / 2;
            item.checkRect.setBounds(checkX, checkY, checkSize, checkSize);

            g2.setColor(item.concluida ? new Color(230, 248, 235) : Color.WHITE);
            g2.fillRoundRect(checkX, checkY, checkSize, checkSize, 6, 6);

            g2.setColor(item.concluida ? COLOR_DARK_GREEN : new Color(190, 190, 190));
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(checkX, checkY, checkSize, checkSize, 6, 6);

            if (item.concluida) {
                g2.setColor(COLOR_DARK_GREEN);
                g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(checkX + 5, checkY + 11, checkX + 9, checkY + 16);
                g2.drawLine(checkX + 9, checkY + 16, checkX + 17, checkY + 6);
            }

            // Texto da tarefa
            g2.setFont(fontCaveat.deriveFont(Font.ITALIC, 17f));
            g2.setColor(item.concluida ? new Color(160, 160, 160) : new Color(35, 35, 35));

            String[] linhas = item.texto.split("\n");
            if (linhas.length > 1) {
                g2.drawString(linhas[0], taskX + 44, curY + 18);
                g2.drawString(linhas[1], taskX + 44, curY + 34);
            } else {
                g2.drawString(item.texto, taskX + 44, curY + 27);
            }

            if (item.concluida) {
                FontMetrics fm = g2.getFontMetrics();
                int txtW = Math.min(taskW - 80, fm.stringWidth(linhas[0]));
                g2.setColor(new Color(160, 160, 160, 180));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawLine(taskX + 44, curY + 23, taskX + 44 + txtW, curY + 23);
            }

            // Botão Remover ✖
            int delSize = 24;
            int delX = taskX + taskW - 30;
            int delY = curY + (itemH - delSize) / 2;
            item.deleteRect.setBounds(delX, delY, delSize, delSize);

            g2.setColor(COLOR_CORAL);
            g2.setFont(fontCabin.deriveFont(16f));
            g2.drawString("✖", delX + 5, delY + 17);

            curY += itemH + itemGap;
        }

        // Caixa de adicionar meta (tracejada) — proporcional ocupando toda a largura
        int addH = 38;
        int btnGap = 8;
        int addW = taskW - (addH * 2 + btnGap * 2);
        int addX = taskX;
        int addY = curY + 4;

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(addX, addY, addW, addH, 14, 14);
        g2.setColor(new Color(180, 200, 220));
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{4, 4}, 0));
        g2.drawRoundRect(addX, addY, addW, addH, 14, 14);

        // Posicionar JTextField exatamente dentro da caixa tracejada
        if (txtInput != null) {
            if (addY < 70 || addY > 672 - 38) {
                txtInput.setVisible(false);
            } else {
                txtInput.setVisible(true);
                txtInput.setBounds(addX + 6, addY + 2, addW - 12, addH - 4);
            }
        }

        // Botão + (cor da pílula da categoria)
        int btnPlusX = addX + addW + btnGap;
        btnPlus.setBounds(btnPlusX, addY, addH, addH);
        g2.setColor(pillColor);
        g2.fillRoundRect(btnPlusX, addY, addH, addH, 12, 12);
        g2.setColor(Color.WHITE);
        g2.setFont(fontCabin.deriveFont(22f));
        FontMetrics fmPlus = g2.getFontMetrics();
        g2.drawString("+", btnPlusX + (addH - fmPlus.stringWidth("+")) / 2, addY + 26);

        // Botão ↺ (cor da pílula da categoria)
        int btnRefX = btnPlusX + addH + btnGap;
        btnRef.setBounds(btnRefX, addY, addH, addH);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(btnRefX, addY, addH, addH, 12, 12);
        g2.setColor(pillColor);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(btnRefX, addY, addH, addH, 12, 12);

        // Ícone vetorial ↺
        desenharIconeRefresh(g2, btnRefX + addH / 2, addY + addH / 2, 7, pillColor);

        return y + cardH;
    }

    private void desenharIconeRefresh(Graphics2D g2, int cx, int cy, int raio, Color cor) {
        Graphics2D g = (Graphics2D) g2.create();
        g.setColor(cor);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Arco circular
        g.drawArc(cx - raio, cy - raio, raio * 2, raio * 2, 45, 270);
        // Ponta da flecha
        int ax = cx + (int)(raio * Math.cos(Math.toRadians(45)));
        int ay = cy - (int)(raio * Math.sin(Math.toRadians(45)));
        g.drawLine(ax - 3, ay - 3, ax + 2, ay);
        g.drawLine(ax - 3, ay + 3, ax + 2, ay);
        g.dispose();
    }

    // ==========================================
    // 5. TELA AJUDA (ESTILO SCRAPBOOK COZY IGUAL AO PERFIL)
    // ==========================================
    private void desenharAjuda(Graphics2D g2, int cx, int w, int h) {
        // Fundo do caderno estilo scrapbook
        GradientPaint ajudaBg = new GradientPaint(cx, 0, new Color(255, 254, 240), cx, h - 78, new Color(255, 250, 226));
        g2.setPaint(ajudaBg);
        g2.fillRect(cx, 0, CONTAINER_W, h - 78);

        // Linhas suaves de caderno pautado
        g2.setColor(new Color(195, 215, 230, 65));
        g2.setStroke(new BasicStroke(0.6f));
        for (int y = 40; y < h - 78; y += 32) {
            g2.drawLine(cx, y, cx + CONTAINER_W, y);
        }

        // Enfeites scrapbook florais e estrelinhas nos cantos
        desenharFlorComFolhas(g2, cx + 24, 22, 13, new Color(255, 185, 215), false, 0.35);
        desenharFlorComFolhas(g2, cx + CONTAINER_W - 28, 24, 13, new Color(185, 225, 195), false, -0.35);
        desenharBrilhoCintilante(g2, cx + 58, 32, 8, new Color(255, 215, 75));
        desenharBrilhoCintilante(g2, cx + CONTAINER_W - 62, 32, 8, new Color(255, 215, 75));

        // ── Título estilizado estilo "MEU PERFIL" ──
        int headCX = cx + CONTAINER_W / 2;
        g2.setFont(fontTitulo.deriveFont(42f));
        FontMetrics fmTit = g2.getFontMetrics();
        String titulo = "AJUDA & DICAS";
        int titX = headCX - fmTit.stringWidth(titulo) / 2;

        // Aura suave pulsante atrás do título
        float glow = (float)(0.7 + 0.3 * Math.sin(animTicks * 2.0));
        g2.setColor(new Color(255, 220, 100, (int)(38 * glow)));
        g2.fillRoundRect(titX - 18, 14, fmTit.stringWidth(titulo) + 36, 50, 24, 24);

        // Outline branco
        g2.setColor(Color.WHITE);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) g2.drawString(titulo, titX + dx, 54 + dy);
            }
        }
        g2.setColor(new Color(175, 115, 30));
        g2.drawString(titulo, titX, 54);

        // Enfeites ao lado do título (iguais ao perfil)
        desenharBrilhoCintilante(g2, titX - 22, 40, 10, new Color(255, 215, 75));
        desenharEstrela(g2, titX - 44, 38, 11, animTicks * 1.4 % 360);
        desenharFlorComFolhas(g2, titX - 74, 48, 12, new Color(255, 200, 120), false, 0.4);
        desenharBrilhoCintilante(g2, titX + fmTit.stringWidth(titulo) + 22, 40, 10, new Color(255, 215, 75));
        desenharEstrela(g2, titX + fmTit.stringWidth(titulo) + 44, 38, 11, -(animTicks * 1.4 % 360));
        desenharFlorComFolhas(g2, titX + fmTit.stringWidth(titulo) + 74, 48, 12, new Color(185, 220, 245), false, -0.4);

        // Washi tapes decorando abaixo do título
        desenharWashiTapePoas(g2, titX - 10, 60, 50, 13, new Color(255, 215, 140, 210), -3);
        desenharFitaScrapbook(g2, titX + fmTit.stringWidth(titulo) - 40, 60, 50, 13, new Color(200, 185, 240, 210), 3);

        // ── Card de Apresentação / Guia (igual ao estilo do botão Nome do perfil) ──
        int centerW = CONTAINER_W - 50;  // quase toda a largura do container
        int centerX = cx + 25;           // margem pequena nas laterais

        int bannerY = 82;
        int bannerH = 36;
        // Sombra
        g2.setColor(new Color(0, 0, 0, 12));
        g2.fillRoundRect(centerX + 2, bannerY + 3, centerW, bannerH, 16, 16);
        // Gradiente creme quente
        GradientPaint banGrad = new GradientPaint(centerX, bannerY, new Color(255, 253, 220), centerX, bannerY + bannerH, new Color(255, 245, 185));
        g2.setPaint(banGrad);
        g2.fillRoundRect(centerX, bannerY, centerW, bannerH, 16, 16);
        // Borda
        g2.setColor(new Color(230, 190, 90));
        g2.setStroke(new BasicStroke(1.3f));
        g2.drawRoundRect(centerX, bannerY, centerW, bannerH, 16, 16);
        // Costura interna
        g2.setColor(new Color(210, 170, 70, 90));
        g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{3, 3}, 0));
        g2.drawRoundRect(centerX + 4, bannerY + 4, centerW - 8, bannerH - 8, 12, 12);
        // Texto
        g2.setFont(fontCabin.deriveFont(Font.BOLD, 13f));
        g2.setColor(new Color(110, 75, 20));
        String txtBanner = "\uD83D\uDCA1 Tire suas d\u00favidas e aproveite ao m\u00e1ximo sua jornada!";
        FontMetrics fmB = g2.getFontMetrics();
        g2.drawString(txtBanner, centerX + (centerW - fmB.stringWidth(txtBanner)) / 2, bannerY + 23);
        desenharBrilhoCintilante(g2, centerX + centerW - 18, bannerY + 14, 6, new Color(255, 215, 75));
        desenharBrilhoCintilante(g2, centerX + 18, bannerY + 14, 6, new Color(255, 215, 75));

        // ── 3 Cards de FAQ estilizados como Scrapbook (largura proporcional centralizada) ──
        String[][] faqs = {
            {"COMO ADICIONO UMA NOVA META?", "V\u00e1 at\u00e9 a aba 'Objetivos', digite sua nova meta na caixinha tracejada e clique no '+' ou aperte Enter no teclado.", "\uD83C\uDFAF", "METAS & MISS\u00d5ES"},
            {"ONDE VEJO MINHAS CONQUISTAS?", "Basta clicar em 'Perfil' no menu inferior e tocar no bot\u00e3o 'CONQUISTAS' para conferir todas as suas medalhas!", "\uD83C\uDFC6", "CONQUISTAS & MEDALHAS"},
            {"COMO GANHO MAIS N\u00cdVEIS E XP?", "Complete suas tarefas na aba 'Objetivos' e fa\u00e7a o check-in di\u00e1rio. O progresso enche as barras de cada hobby!", "\uD83D\uDCC8", "N\u00cdVEIS & PROGRESSO"}
        };

        Color[] cardBg1 = {
            new Color(248, 252, 255), // azul bebê
            new Color(255, 253, 240), // creme dourado
            new Color(246, 255, 248)  // verde menta
        };
        Color[] cardBg2 = {
            new Color(228, 240, 255),
            new Color(255, 245, 215),
            new Color(230, 252, 236)
        };
        Color[] bordas = {
            new Color(160, 195, 235),
            new Color(235, 195, 115),
            new Color(150, 215, 175)
        };
        Color[] titulos = {
            new Color(40, 95, 165),
            new Color(160, 100, 25),
            new Color(45, 125, 75)
        };

        int cardY = 126;
        int gap = 10;

        for (int i = 0; i < faqs.length; i++) {
            String pergunta = faqs[i][0];
            String resposta = faqs[i][1];
            String emoji = faqs[i][2];
            String tag = faqs[i][3];

            // Pré-medir o texto para calcular a altura do card
            g2.setFont(fontCaveat.deriveFont(Font.ITALIC, 16.5f));
            FontMetrics fmResp = g2.getFontMetrics();
            int seloSize = 40;
            int contentX = centerX + 18 + seloSize + 14;
            int contentW = centerW - (contentX - centerX) - 18;
            List<String> linhas = quebraTexto(resposta, fmResp, contentW);
            // Altura: cabeçalho (45px) + linhas texto (18px cada) + padding inferior (16px)
            int cardH = 45 + linhas.size() * 18 + 16;
            cardH = Math.max(cardH, 82); // mínimo de altura

            // Sombra suave multicamada
            g2.setColor(new Color(0, 0, 0, 12));
            g2.fillRoundRect(centerX + 3, cardY + 4, centerW, cardH, 20, 20);
            g2.setColor(new Color(0, 0, 0, 8));
            g2.fillRoundRect(centerX + 1, cardY + 2, centerW, cardH, 20, 20);

            // Fundo gradiente pastel
            GradientPaint cGrad = new GradientPaint(centerX, cardY, cardBg1[i], centerX, cardY + cardH, cardBg2[i]);
            g2.setPaint(cGrad);
            g2.fillRoundRect(centerX, cardY, centerW, cardH, 20, 20);

            // Borda elegante
            g2.setColor(bordas[i]);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(centerX, cardY, centerW, cardH, 20, 20);

            // Costura pontilhada interna (estilo scrapbook)
            g2.setColor(new Color(bordas[i].getRed(), bordas[i].getGreen(), bordas[i].getBlue(), 90));
            g2.setStroke(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{4, 4}, 0));
            g2.drawRoundRect(centerX + 5, cardY + 5, centerW - 10, cardH - 10, 16, 16);

            // Fita washi tape decorativa no cantinho superior esquerdo
            if (i == 0) {
                desenharWashiTapePoas(g2, centerX + 12, cardY - 6, 42, 12, new Color(180, 210, 255, 220), -2);
            } else if (i == 1) {
                desenharFitaScrapbook(g2, centerX + 12, cardY - 6, 42, 12, new Color(255, 220, 140, 220), 2);
            } else {
                desenharWashiTapePoas(g2, centerX + 12, cardY - 6, 42, 12, new Color(190, 240, 205, 220), -2);
            }

            // Ícone circular (Selo Scrapbook)
            int seloX = centerX + 18;
            int seloY = cardY + cardH / 2 - 20;
            // seloSize já definido no pré-passo acima
            // Sombra do selo
            g2.setColor(new Color(0, 0, 0, 10));
            g2.fillOval(seloX + 1, seloY + 2, seloSize, seloSize);
            // Fundo branco do selo
            g2.setColor(Color.WHITE);
            g2.fillOval(seloX, seloY, seloSize, seloSize);
            // Borda do selo
            g2.setColor(bordas[i]);
            g2.setStroke(new BasicStroke(1.3f));
            g2.drawOval(seloX, seloY, seloSize, seloSize);
            // Emoji centralizado
            g2.setFont(new Font("SansSerif", Font.PLAIN, 20));
            FontMetrics fmE = g2.getFontMetrics();
            g2.drawString(emoji, seloX + (seloSize - fmE.stringWidth(emoji)) / 2, seloY + seloSize / 2 + 7);

            // Conteúdo textual – reutiliza contentX/contentW já calculados no pré-passo
            // Mini Badge de Categoria
            g2.setFont(fontCabin.deriveFont(Font.BOLD, 10f));
            g2.setColor(titulos[i]);
            g2.drawString("\u2605 " + tag, contentX, cardY + 21);

            // Pergunta (Título)
            g2.setFont(fontCabin.deriveFont(Font.BOLD, 13.5f));
            g2.setColor(new Color(50, 45, 40));
            g2.drawString(pergunta, contentX, cardY + 38);

            // Linha decorativa
            g2.setColor(new Color(bordas[i].getRed(), bordas[i].getGreen(), bordas[i].getBlue(), 120));
            g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{3, 3}, 0));
            g2.drawLine(contentX, cardY + 44, contentX + contentW - 10, cardY + 44);

            // Resposta em caligrafia limpa – todas as linhas (sem truncar)
            g2.setFont(fontCaveat.deriveFont(Font.ITALIC, 16.5f));
            g2.setColor(new Color(75, 65, 60));
            int textY = cardY + 62;
            for (int l = 0; l < linhas.size(); l++) {
                g2.drawString(linhas.get(l), contentX, textY + l * 18);
            }

            // Estrelinha brilhante no canto superior direito do card
            desenharBrilhoCintilante(g2, centerX + centerW - 18, cardY + 16, 5, new Color(255, 215, 75));

            cardY += cardH + gap;
        }

        // ── Nota Scrapbook "Dica de Ouro" (Post-It inferior com washi tape) ──
        int noteY = cardY + 2;
        int noteH = 58;

        if (noteY + noteH <= h - 84) {
            // Sombra
            g2.setColor(new Color(0, 0, 0, 10));
            g2.fillRoundRect(centerX + 3, noteY + 3, centerW, noteH, 16, 16);

            // Fundo amarelo post-it
            GradientPaint postGrad = new GradientPaint(centerX, noteY, new Color(255, 252, 230), centerX, noteY + noteH, COLOR_POSTIT_YELLOW);
            g2.setPaint(postGrad);
            g2.fillRoundRect(centerX, noteY, centerW, noteH, 16, 16);

            // Borda pontilhada
            g2.setColor(new Color(220, 185, 95));
            g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{4, 4}, 0));
            g2.drawRoundRect(centerX, noteY, centerW, noteH, 16, 16);

            // Fita washi tape central segurando o post-it
            desenharFitaScrapbook(g2, centerX + centerW / 2 - 32, noteY - 7, 64, 15, new Color(255, 210, 140, 210), -1);

            // Ícone da lâmpada
            int lampCX = centerX + 32;
            int lampCY = noteY + noteH / 2;
            g2.setColor(Color.WHITE);
            g2.fillOval(lampCX - 16, lampCY - 16, 32, 32);
            g2.setColor(new Color(230, 190, 80));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawOval(lampCX - 16, lampCY - 16, 32, 32);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
            FontMetrics fmLamp = g2.getFontMetrics();
            String lamp = "\uD83D\uDCA1";
            g2.drawString(lamp, lampCX - fmLamp.stringWidth(lamp) / 2, lampCY + 6);

            // Texto da dica
            g2.setFont(fontCabin.deriveFont(Font.BOLD, 11.5f));
            g2.setColor(new Color(145, 95, 20));
            g2.drawString("DICA DE OURO DO HOBBY QUEST", centerX + 58, noteY + 22);

            g2.setFont(fontCaveat.deriveFont(Font.ITALIC, 15f));
            g2.setColor(new Color(85, 65, 35));
            String dicaTexto = "Acesse todos os dias para manter sua ofensiva e desbloquear conquistas!";
            g2.drawString(dicaTexto, centerX + 58, noteY + 42);

            desenharBrilhoCintilante(g2, centerX + centerW - 20, noteY + noteH / 2, 6, new Color(255, 210, 70));
        }
    }

    private List<String> quebraTexto(String texto, FontMetrics fm, int maxW) {
        List<String> linhas = new ArrayList<>();
        String[] palavras = texto.split(" ");
        StringBuilder linhaAtual = new StringBuilder();

        for (String palavra : palavras) {
            String teste = linhaAtual.length() > 0 ? linhaAtual + " " + palavra : palavra;
            if (fm.stringWidth(teste) > maxW && linhaAtual.length() > 0) {
                linhas.add(linhaAtual.toString());
                linhaAtual = new StringBuilder(palavra);
            } else {
                linhaAtual = new StringBuilder(teste);
            }
        }
        if (linhaAtual.length() > 0) linhas.add(linhaAtual.toString());
        return linhas;
    }

    /** Encurta um texto com reticências para caber em uma largura máxima. */
    private String encurtarTexto(String texto, FontMetrics fm, int maxW) {
        if (texto == null || fm.stringWidth(texto) <= maxW) return texto;
        String reticencias = "...";
        int limite = maxW - fm.stringWidth(reticencias);
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) {
            if (fm.stringWidth(sb.toString() + c) > limite) break;
            sb.append(c);
        }
        return sb + reticencias;
    }

    // ==========================================
    // 6. TELA PERFIL
    // ==========================================
    private void desenharPerfil(Graphics2D g2, int cx, int w, int h) {
        // ── Título "MEU PERFIL" estilizado ──
        int headCX = cx + CONTAINER_W / 2;
        g2.setFont(fontTitulo.deriveFont(42f));
        FontMetrics fmTit = g2.getFontMetrics();
        String titulo = "MEU PERFIL";
        int titX = headCX - fmTit.stringWidth(titulo) / 2;

        // Aura suave pulsante atrás do título
        float glow = (float)(0.7 + 0.3 * Math.sin(animTicks * 2.0));
        g2.setColor(new Color(185, 155, 230, (int)(30 * glow)));
        g2.fillRoundRect(titX - 18, 14, fmTit.stringWidth(titulo) + 36, 50, 24, 24);

        // Outline branco
        g2.setColor(Color.WHITE);
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) {
            if (dx!=0||dy!=0) g2.drawString(titulo, titX + dx, 54 + dy);
        }
        g2.setColor(new Color(130, 90, 185));
        g2.drawString(titulo, titX, 54);

        // Enfeites ao lado do título
        desenharBrilhoCintilante(g2, titX - 22, 40, 10, new Color(255, 215, 75));
        desenharEstrela(g2, titX - 44, 38, 11, animTicks * 1.4 % 360);
        desenharFlorComFolhas(g2, titX - 72, 48, 12, new Color(248, 185, 215), false, 0.4);
        desenharBrilhoCintilante(g2, titX + fmTit.stringWidth(titulo) + 22, 40, 10, new Color(255, 215, 75));
        desenharEstrela(g2, titX + fmTit.stringWidth(titulo) + 44, 38, 11, -(animTicks * 1.4 % 360));
        desenharFlorComFolhas(g2, titX + fmTit.stringWidth(titulo) + 72, 48, 12, new Color(185, 220, 185), false, -0.4);

        // Washi tapes abaixo do título
        desenharWashiTapePoas(g2, titX - 10, 60, 50, 13, new Color(255, 215, 140, 200), -3);
        desenharFitaScrapbook(g2, titX + fmTit.stringWidth(titulo) - 40, 60, 50, 13, new Color(200, 185, 240, 200), 3);

        // ── Polaroid flutuante com washi tape ──
        AffineTransform old = g2.getTransform();
        int polW = 182;
        int polH = 214;
        int polX = headCX - polW / 2;
        // Flutuação suave do polaroid
        int floatPol = (int)(Math.sin(animTicks * 2.0) * 2.5);
        g2.translate(polX, 58 + floatPol);
        g2.rotate(Math.toRadians(-2));

        // Aura luminosa ao redor do polaroid
        Composite polComp = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        g2.setColor(new Color(200, 180, 240));
        g2.fillRoundRect(-8, -8, polW + 16, polH + 16, 18, 18);
        g2.setComposite(polComp);

        // Sombra do polaroid (multicamadas)
        g2.setColor(new Color(0, 0, 0, 18));
        g2.fillRect(5, 6, polW, polH);
        g2.setColor(new Color(0, 0, 0, 10));
        g2.fillRect(3, 4, polW, polH);

        // Borda branca do polaroid
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, polW, polH);
        // Destaque sutil nas bordas
        g2.setColor(new Color(240, 235, 250));
        g2.fillRect(0, 0, 3, polH);
        g2.fillRect(0, 0, polW, 3);

        // Área da foto com cor de fundo pastel
        int fotoW = polW - 22;
        int fotoH = polH - 46;
        g2.setColor(new Color(240, 240, 248));
        g2.fillRect(11, 11, fotoW, fotoH);

        if (fotoCustomizada != null) {
            // Foto do PC com smart cover crop
            Rectangle b = avatarLimiteCustom;
            if (b == null) b = new Rectangle(0, 0, fotoCustomizada.getWidth(), fotoCustomizada.getHeight());
            double boxSize = Math.min(b.width, b.height);
            double srcX = b.x + (b.width - boxSize) / 2.0;
            double srcY = b.y;
            if (srcX < 0) srcX = 0;
            if (srcY + boxSize > fotoCustomizada.getHeight()) srcY = fotoCustomizada.getHeight() - boxSize;
            Shape oldClip2 = g2.getClip();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setClip(new Rectangle(11, 11, fotoW, fotoH));
            g2.drawImage(fotoCustomizada, 11, 11, 11 + fotoW, 11 + fotoH,
                (int)srcX, (int)srcY, (int)(srcX + boxSize), (int)(srcY + boxSize), null);
            g2.setClip(oldClip2);
        } else if (avatarImagens[avatarSelecionado] != null) {
            desenharAvatarProporcional(g2, avatarSelecionado, 11, 11, fotoW, fotoH, false);
        } else {
            g2.setColor(COLOR_PURPLE_BADGE);
            g2.fillOval(fotoW / 2 - 25 + 11, fotoH / 2 - 25 + 11, 50, 50);
            g2.setColor(Color.WHITE);
            g2.setFont(fontTitulo.deriveFont(32f));
            g2.drawString("A", fotoW / 2 - 10 + 11, fotoH / 2 + 10 + 11);
        }

        // Mini estrelinhas decorando os cantos da foto
        g2.setColor(new Color(255, 215, 75, 180));
        g2.fillOval(7, 7, 5, 5);
        g2.fillOval(polW - 13, 8, 5, 5);

        // Nome abaixo da foto em caligrafia
        g2.setFont(fontCaveat.deriveFont(Font.ITALIC, 16f));
        g2.setColor(new Color(90, 70, 130));
        FontMetrics fmP = g2.getFontMetrics();
        g2.drawString(nomeJogador, (polW - fmP.stringWidth(nomeJogador)) / 2, polH - 10);

        // Washi tape colorida segurando o polaroid
        desenharFitaScrapbook(g2, 18, -10, 90, 18, new Color(185, 185, 240, 190), 0);

        g2.setTransform(old);

        // ── Botão Trocar Nome estilizado ──
        int tnY = btnTrocarNome.y;
        // Sombra
        g2.setColor(new Color(0, 0, 0, 10));
        g2.fillRoundRect(btnTrocarNome.x + 2, tnY + 3, btnTrocarNome.width, btnTrocarNome.height, 16, 16);
        // Fundo gradiente amarelo
        GradientPaint tnGrad = new GradientPaint(btnTrocarNome.x, tnY, new Color(255, 253, 215), btnTrocarNome.x, tnY + btnTrocarNome.height, new Color(250, 240, 185));
        g2.setPaint(tnGrad);
        g2.fillRoundRect(btnTrocarNome.x, tnY, btnTrocarNome.width, btnTrocarNome.height, 16, 16);
        g2.setColor(COLOR_GOLD_STAR);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(btnTrocarNome.x, tnY, btnTrocarNome.width, btnTrocarNome.height, 16, 16);
        // Costura interna
        g2.setColor(new Color(220, 200, 100, 100));
        g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{3, 3}, 0));
        g2.drawRoundRect(btnTrocarNome.x + 4, tnY + 4, btnTrocarNome.width - 8, btnTrocarNome.height - 8, 12, 12);
        g2.setFont(fontCabin.deriveFont(15f));
        g2.setColor(new Color(60, 50, 20));
        g2.drawString("\u270F\uFE0F Nome: " + nomeJogador, btnTrocarNome.x + 25, tnY + 23);
        // Estrelinha decorativa dentro do botão
        desenharBrilhoCintilante(g2, btnTrocarNome.x + btnTrocarNome.width - 20, tnY + 14, 6, new Color(255, 200, 60));

        // ── "Escolha seu Personagem" com decoração ──
        int avatarTotalW = 7 * 50 + 6 * 10;
        int avatarStartX = cx + (CONTAINER_W - avatarTotalW) / 2;
        g2.setFont(fontCabin.deriveFont(Font.BOLD, 15f));
        g2.setColor(COLOR_GREEN_TITLE);
        g2.drawString("Escolha seu Personagem:", avatarStartX, 296);
        desenharBrilhoCintilante(g2, avatarStartX - 16, 290, 6, new Color(130, 200, 140));

        // Avatares em círculos com brilho no selecionado
        for (int i = 0; i < 7; i++) {
            Rectangle ab = avatarBotoes[i];
            int abY = ab.y;
            boolean sel = (avatarSelecionado == i);

            // Brilho animado no avatar selecionado
            if (sel) {
                float selGlow = (float)(0.6 + 0.4 * Math.sin(animTicks * 3.0));
                Composite selComp = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, selGlow * 0.4f));
                g2.setColor(new Color(100, 215, 130));
                g2.fillOval(ab.x - 4, abY - 4, ab.width + 8, ab.height + 8);
                g2.setComposite(selComp);
            }

            // Fundo do círculo
            g2.setColor(sel ? new Color(190, 230, 185) : new Color(250, 250, 255));
            g2.fillOval(ab.x, abY, ab.width, ab.height);
            // Borda
            g2.setColor(sel ? COLOR_DARK_GREEN : new Color(200, 200, 215));
            g2.setStroke(new BasicStroke(sel ? 2.5f : 1.2f));
            g2.drawOval(ab.x, abY, ab.width, ab.height);

            if (avatarImagens[i] != null) {
                desenharAvatarProporcional(g2, i, ab.x + 3, abY + 3, ab.width - 6, ab.height - 6, true);
            }

            // Estrelinha no avatar selecionado
            if (sel) {
                desenharBrilhoCintilante(g2, ab.x + ab.width - 2, abY, 6, new Color(255, 215, 75));
            }
        }

        // ── Botão "📷 Enviar minha foto" ──
        {
            int upX = btnUploadFoto.x, upY = btnUploadFoto.y, upW = btnUploadFoto.width, upH = btnUploadFoto.height;
            // Sombra
            g2.setColor(new Color(0, 0, 0, 12));
            g2.fillRoundRect(upX + 2, upY + 3, upW, upH, 20, 20);
            // Gradiente lilás
            GradientPaint upGrad = new GradientPaint(upX, upY, new Color(230, 205, 255), upX, upY + upH, new Color(195, 165, 240));
            g2.setPaint(upGrad);
            g2.fillRoundRect(upX, upY, upW, upH, 20, 20);
            // Brilho interno topo
            g2.setColor(new Color(255, 255, 255, 60));
            g2.fillRoundRect(upX + 3, upY + 2, upW - 6, upH / 2, 18, 18);
            // Borda
            g2.setColor(new Color(150, 110, 200));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(upX, upY, upW, upH, 20, 20);
            // Texto
            g2.setFont(fontCabin.deriveFont(13f));
            g2.setColor(new Color(60, 20, 90));
            String txtUp = fotoCustomizada != null ? "\u274c Remover minha foto" : "\uD83D\uDCF7 Enviar minha foto";
            FontMetrics fmUp = g2.getFontMetrics();
            g2.drawString(txtUp, upX + (upW - fmUp.stringWidth(txtUp)) / 2, upY + upH / 2 + 5);
        }

        // ── Card de ofensiva e Botão Conquistas estilizados ──
        int cardRowW = Math.min(CONTAINER_W - 60, 520);
        int cardRowX = cx + (CONTAINER_W - cardRowW) / 2;
        int colItemW = (cardRowW - 20) / 2;
        int offY = btnAbrirConquistas.y;

        // Sombra card ofensiva
        g2.setColor(new Color(0, 0, 0, 10));
        g2.fillRoundRect(cardRowX + 2, offY + 3, colItemW, btnAbrirConquistas.height, 16, 16);
        // Gradiente amarelo quente
        GradientPaint offGrad = new GradientPaint(cardRowX, offY, new Color(255, 255, 210), cardRowX, offY + btnAbrirConquistas.height, COLOR_POSTIT_YELLOW);
        g2.setPaint(offGrad);
        g2.fillRoundRect(cardRowX, offY, colItemW, btnAbrirConquistas.height, 16, 16);
        g2.setColor(new Color(220, 200, 100));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(cardRowX, offY, colItemW, btnAbrirConquistas.height, 16, 16);
        g2.setColor(new Color(60, 50, 20));
        g2.setFont(fontCabin.deriveFont(Font.BOLD, 15f));
        FontMetrics fmOf = g2.getFontMetrics();
        String txtOf = "\uD83D\uDD25 Ofensiva: " + streakDias + " Dias";
        g2.drawString(txtOf, cardRowX + (colItemW - fmOf.stringWidth(txtOf)) / 2, offY + 28);

        // Botão Conquistas estilizado
        g2.setColor(new Color(0, 0, 0, 10));
        g2.fillRoundRect(btnAbrirConquistas.x + 2, offY + 3, btnAbrirConquistas.width, btnAbrirConquistas.height, 16, 16);
        GradientPaint conquGrad = new GradientPaint(btnAbrirConquistas.x, offY, new Color(100, 160, 240), btnAbrirConquistas.x, offY + btnAbrirConquistas.height, COLOR_BLUE_LINK);
        g2.setPaint(conquGrad);
        g2.fillRoundRect(btnAbrirConquistas.x, offY, btnAbrirConquistas.width, btnAbrirConquistas.height, 16, 16);
        g2.setColor(new Color(255, 255, 255, 60));
        g2.fillRoundRect(btnAbrirConquistas.x + 3, offY + 2, btnAbrirConquistas.width - 6, 18, 14, 14);
        g2.setColor(Color.WHITE);
        g2.setFont(fontCabin.deriveFont(Font.BOLD, 15f));
        FontMetrics fmC = g2.getFontMetrics();
        String txtConq = "\uD83C\uDFC6 CONQUISTAS";
        g2.drawString(txtConq, btnAbrirConquistas.x + (btnAbrirConquistas.width - fmC.stringWidth(txtConq)) / 2, offY + 28);

        // ── Botão Logout ──
        int logY = btnLogout.y;
        g2.setColor(new Color(0, 0, 0, 8));
        g2.fillRoundRect(btnLogout.x + 2, logY + 3, btnLogout.width, btnLogout.height, 14, 14);
        GradientPaint logGrad = new GradientPaint(btnLogout.x, logY, new Color(255, 245, 240), btnLogout.x, logY + btnLogout.height, new Color(255, 230, 220));
        g2.setPaint(logGrad);
        g2.fillRoundRect(btnLogout.x, logY, btnLogout.width, btnLogout.height, 14, 14);
        g2.setColor(COLOR_CORAL);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(btnLogout.x, logY, btnLogout.width, btnLogout.height, 14, 14);
        g2.setFont(fontCabin.deriveFont(15f));
        FontMetrics fmL = g2.getFontMetrics();
        String txtLog = "\uD83D\uDEAA SAIR DA CONTA (LOGOUT)";
        g2.setColor(new Color(190, 60, 50));
        g2.drawString(txtLog, btnLogout.x + (btnLogout.width - fmL.stringWidth(txtLog)) / 2, logY + 25);
    }

    // ==========================================
    // OVERLAY CONQUISTAS
    // ==========================================
    private void desenharOverlayConquistas(Graphics2D g2, int w, int h) {
        // Fundo escurecido semitransparente
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, w, h);

        // Card central estilo scrapbook
        int cW = Math.min(w - 60, 480);
        int cH = 340;
        int cX = (w - cW) / 2;
        int cY = (h - cH) / 2 - 20;

        // Sombra do card
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillRoundRect(cX + 6, cY + 8, cW, cH, 24, 24);

        // Fundo do card — papel creme
        GradientPaint cardBg = new GradientPaint(cX, cY, new Color(255, 253, 240), cX, cY + cH, new Color(255, 248, 220));
        g2.setPaint(cardBg);
        g2.fillRoundRect(cX, cY, cW, cH, 24, 24);

        // Borda pontilhada decorativa
        g2.setColor(new Color(200, 180, 140, 180));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            0, new float[]{6, 5}, 0));
        g2.drawRoundRect(cX + 8, cY + 8, cW - 16, cH - 16, 18, 18);
        g2.setStroke(new BasicStroke(1f));

        // Washi tape no topo
        g2.setColor(new Color(255, 200, 100, 180));
        g2.fillRoundRect(cX + cW / 2 - 40, cY - 10, 80, 20, 8, 8);
        g2.setColor(new Color(255, 170, 60, 100));
        for (int px = cX + cW / 2 - 38; px < cX + cW / 2 + 40; px += 10) {
            g2.fillOval(px, cY - 7, 6, 6);
        }

        // Título "🏆 Conquistas"
        g2.setFont(fontTitulo.deriveFont(Font.BOLD, 22f));
        g2.setColor(new Color(120, 80, 20));
        String titulo = "\uD83C\uDFC6 Conquistas";
        FontMetrics fmT = g2.getFontMetrics();
        g2.drawString(titulo, cX + (cW - fmT.stringWidth(titulo)) / 2, cY + 50);

        // Linha separadora decorativa
        g2.setColor(new Color(220, 190, 130, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(cX + 24, cY + 60, cX + cW - 24, cY + 60);
        g2.setStroke(new BasicStroke(1f));

        // Itens de conquistas
        String[][] itens = {
            {"\u2B50", "Primeira Jornada", "Desbloqueada!", "1"},
            {"\uD83D\uDCDA", "Leitora \u00c1vida", "XP: " + xpLeitura + " / 100", String.valueOf(xpLeitura)},
            {"\uD83C\uDFA8", "Artista Criativa", "XP: " + xpArte + " / 100", String.valueOf(xpArte)},
            {"\uD83C\uDFCB\uFE0F", "Atleta Focada", "XP: " + xpFitness + " / 100", String.valueOf(xpFitness)}
        };

        int itemX = cX + 24;
        int itemY = cY + 80;
        int itemH = 50;
        int barMaxW = cW - 120;

        for (String[] item : itens) {
            String emoji = item[0];
            String nome  = item[1];
            String desc  = item[2];
            int xp = 0;
            try { xp = Integer.parseInt(item[3]); } catch (Exception ignored) {}
            boolean desbloqueada = xp > 0 || item[3].equals("1");

            // Fundo do item
            g2.setColor(desbloqueada ? new Color(255, 245, 200, 180) : new Color(230, 230, 230, 120));
            g2.fillRoundRect(itemX, itemY, cW - 48, itemH - 4, 12, 12);
            if (desbloqueada) {
                g2.setColor(new Color(220, 180, 80, 120));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(itemX, itemY, cW - 48, itemH - 4, 12, 12);
            }

            // Emoji
            g2.setFont(fontCabin.deriveFont(20f));
            g2.setColor(new Color(80, 60, 20));
            g2.drawString(emoji, itemX + 10, itemY + 30);

            // Nome
            g2.setFont(fontCabin.deriveFont(Font.BOLD, 13f));
            g2.setColor(desbloqueada ? new Color(100, 65, 10) : new Color(140, 140, 140));
            g2.drawString(nome, itemX + 44, itemY + 18);

            // Barra de progresso (só para itens com XP)
            if (!item[3].equals("1")) {
                int barW = (int)(barMaxW * Math.min(xp, 100) / 100.0);
                // Trilha
                g2.setColor(new Color(200, 190, 170, 180));
                g2.fillRoundRect(itemX + 44, itemY + 24, barMaxW, 10, 6, 6);
                // Preenchimento
                if (barW > 0) {
                    GradientPaint barGrad = new GradientPaint(itemX + 44, itemY + 24,
                        new Color(255, 180, 60), itemX + 44 + barW, itemY + 24, new Color(255, 120, 80));
                    g2.setPaint(barGrad);
                    g2.fillRoundRect(itemX + 44, itemY + 24, barW, 10, 6, 6);
                }
                // Texto %
                g2.setFont(fontCabin.deriveFont(10f));
                g2.setColor(new Color(100, 70, 20));
                g2.drawString(desc, itemX + 44 + barMaxW + 6, itemY + 33);
            } else {
                // Badge "✓ Conquistada"
                g2.setColor(new Color(80, 170, 80));
                g2.fillRoundRect(itemX + 44, itemY + 24, 90, 12, 6, 6);
                g2.setFont(fontCabin.deriveFont(Font.BOLD, 10f));
                g2.setColor(Color.WHITE);
                g2.drawString("\u2713 Conquistada!", itemX + 48, itemY + 34);
            }

            itemY += itemH;
        }

        // Botão fechar — X no canto
        int bFX = cX + cW - 36;
        int bFY = cY + 10;
        int bFSz = 26;
        btnFecharConquistas.setBounds(bFX, bFY, bFSz, bFSz);
        g2.setColor(new Color(220, 90, 70));
        g2.fillOval(bFX, bFY, bFSz, bFSz);
        g2.setColor(new Color(180, 50, 30));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(bFX, bFY, bFSz, bFSz);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Color.WHITE);
        g2.drawLine(bFX + 7, bFY + 7, bFX + bFSz - 7, bFY + bFSz - 7);
        g2.drawLine(bFX + bFSz - 7, bFY + 7, bFX + 7, bFY + bFSz - 7);
        g2.setStroke(new BasicStroke(1f));

        // Decorações de fundo do card
        g2.setColor(new Color(255, 200, 100, 60));
        g2.fillOval(cX + cW - 30, cY + cH - 30, 25, 25);
        g2.setColor(new Color(180, 220, 180, 60));
        g2.fillOval(cX + 10, cY + cH - 25, 18, 18);
    }

    // ==========================================
    // BARRA DE NAVEGAÇÃO INFERIOR
    // ==========================================
    private void desenharBarraNavegacao(Graphics2D g2, int cx, int w, int h) {
        int navH = 78;
        int navY = h - navH;

        // Sombra superior suave — multicamadas
        for (int i = 0; i < 6; i++) {
            g2.setColor(new Color(0, 0, 0, 15 - i * 2));
            g2.fillRect(0, navY - 6 + i, w, 1);
        }

        // Fundo creme suave com textura de papel
        GradientPaint navBg = new GradientPaint(0, navY, new Color(255, 254, 250), 0, navY + navH, new Color(250, 247, 240));
        g2.setPaint(navBg);
        g2.fillRect(0, navY, w, navH);

        // Borda superior estilo washi tape / linha tracejada delicada
        g2.setColor(new Color(103, 167, 96, 120));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(0, navY, w, navY);

        g2.setColor(new Color(220, 210, 195, 160));
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{4, 4}, 0));
        g2.drawLine(0, navY + 2, w, navY + 2);

        int tabW = w / 5;
        // Ícones SVG-like desenhados manualmente
        String[] labels = {"HOME", "OBJETIVOS", "DESEMPENHO", "AJUDA", "PERFIL"};

        for (int i = 0; i < 5; i++) {
            boolean active = (telaAtual == i);
            boolean hovered = (hoverTab == i);
            int tx = i * tabW;
            int tabCenter = tx + tabW / 2;

            // Highlight do item ativo com pílula suave estilo scrapbook
            if (active) {
                int pillW = Math.min(tabW - 14, 110);
                int pillH = navH - 18;
                int pillX = tabCenter - pillW / 2;
                int pillY = navY + 9;

                // Sombra suave da pílula ativa
                g2.setColor(new Color(80, 145, 75, 25));
                g2.fillRoundRect(pillX + 1, pillY + 2, pillW, pillH, 16, 16);

                // Fundo da pílula ativa
                GradientPaint pillGrad = new GradientPaint(pillX, pillY, new Color(236, 248, 238), pillX, pillY + pillH, new Color(220, 242, 224));
                g2.setPaint(pillGrad);
                g2.fillRoundRect(pillX, pillY, pillW, pillH, 16, 16);

                // Borda suave da pílula
                g2.setColor(new Color(130, 190, 140, 150));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(pillX, pillY, pillW, pillH, 16, 16);

                // Indicador topo com estrela/brilho cintilante animado
                desenharBrilhoCintilante(g2, tabCenter, navY + 5, 5, new Color(255, 215, 75));
            } else if (hovered) {
                // Hover suave no tab
                int pillW = Math.min(tabW - 20, 100);
                int pillH = navH - 22;
                int pillX = tabCenter - pillW / 2;
                int pillY = navY + 11;
                g2.setColor(new Color(103, 167, 96, 22));
                g2.fillRoundRect(pillX, pillY, pillW, pillH, 14, 14);
            }

            boolean isTabClicked = (clickRect == navTabs[i] && clickScale != 1.0f);
            AffineTransform tabOld = null;
            if (isTabClicked) {
                tabOld = g2.getTransform();
                g2.translate(tabCenter, navY + navH / 2.0);
                g2.scale(clickScale, clickScale);
                g2.translate(-tabCenter, -(navY + navH / 2.0));
            }

            Color iconColor = active ? COLOR_DARK_GREEN : (hovered ? new Color(60, 145, 60) : new Color(115, 160, 120));

            // Desenhar ícones como formas geométricas (mais fiéis que emojis)
            g2.setColor(iconColor);
            g2.setStroke(new BasicStroke(active ? 2.2f : 1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int iconY = navY + 18;

            switch (i) {
                case 0 -> desenharIconeHome(g2, tabCenter, iconY);
                case 1 -> desenharIconeObjetivos(g2, tabCenter, iconY);
                case 2 -> desenharIconeDesempenho(g2, tabCenter, iconY);
                case 3 -> desenharIconeAjuda(g2, tabCenter, iconY);
                case 4 -> desenharIconePerfil(g2, tabCenter, iconY);
            }

            // Label
            g2.setColor(active ? COLOR_DARK_GREEN : (hovered ? new Color(60, 145, 60) : new Color(110, 140, 115)));
            g2.setFont(fontCabin.deriveFont(active ? Font.BOLD : Font.PLAIN, active ? 12f : 11f));
            FontMetrics fmTab = g2.getFontMetrics();
            g2.drawString(labels[i], tabCenter - fmTab.stringWidth(labels[i]) / 2, navY + 61);

            if (tabOld != null) g2.setTransform(tabOld);
        }
    }

    // Ícones vetoriais desenhados manualmente (sem emoji)
    private void desenharIconeHome(Graphics2D g2, int cx, int y) {
        // Telhado da casa
        Path2D.Float telhado = new Path2D.Float();
        telhado.moveTo(cx - 10, y + 10);
        telhado.lineTo(cx, y + 1);
        telhado.lineTo(cx + 10, y + 10);
        g2.draw(telhado);

        // Paredes e base da casa
        Path2D.Float paredes = new Path2D.Float();
        paredes.moveTo(cx - 8, y + 10);
        paredes.lineTo(cx - 8, y + 21);
        paredes.lineTo(cx + 8, y + 21);
        paredes.lineTo(cx + 8, y + 10);
        g2.draw(paredes);

        // Porta centralizada e perfeitamente nivelada na base (sem vazar para fora)
        Path2D.Float porta = new Path2D.Float();
        porta.moveTo(cx - 3, y + 21);
        porta.lineTo(cx - 3, y + 14);
        porta.lineTo(cx + 3, y + 14);
        porta.lineTo(cx + 3, y + 21);
        g2.draw(porta);
    }

    private void desenharIconeObjetivos(Graphics2D g2, int cx, int y) {
        // Alvo (target)
        int r = 11;
        g2.drawOval(cx - r, y + 2, r * 2, r * 2);
        g2.drawOval(cx - r + 4, y + 6, r * 2 - 8, r * 2 - 8);
        g2.fillOval(cx - 3, y + r - 1, 6, 6);
    }

    private void desenharIconeDesempenho(Graphics2D g2, int cx, int y) {
        // Gráfico de barras
        g2.fillRect(cx - 9, y + 12, 5, 10);
        g2.fillRect(cx - 2, y + 6, 5, 16);
        g2.fillRect(cx + 5, y + 2, 5, 20);
    }

    private void desenharIconeAjuda(Graphics2D g2, int cx, int y) {
        // Círculo externo
        int r = 11;
        int circleY = y + 2;
        g2.drawOval(cx - r, circleY, r * 2, r * 2);

        // Ponto de interrogação perfeitamente centralizado no círculo
        Font fontAntiga = g2.getFont();
        Font fontQ = new Font("SansSerif", Font.BOLD, 15);
        g2.setFont(fontQ);
        FontMetrics fm = g2.getFontMetrics();
        int qx = cx - fm.stringWidth("?") / 2;
        int qy = circleY + (r * 2 + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString("?", qx, qy);
        g2.setFont(fontAntiga);
    }

    private void desenharIconePerfil(Graphics2D g2, int cx, int y) {
        // Silhueta de pessoa
        g2.drawOval(cx - 5, y + 2, 10, 10);
        g2.drawArc(cx - 10, y + 12, 20, 16, 0, 180);
    }

    // ==========================================
    // LÓGICA DE LOGIN / CADASTRO
    // ==========================================
    private void realizarLoginOuCadastro() {
        String email = txtEmail.getText().trim();
        String senha = new String(txtSenha.getPassword()).trim();

        if (modoCadastro) {
            String nome = txtNomeCadastro.getText().trim();
            String conf = new String(txtConfirmaSenha.getPassword()).trim();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos para criar sua conta!", "Hobby Quest", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!senha.equals(conf)) {
                JOptionPane.showMessageDialog(this, "As senhas digitadas não coincidem!", "Hobby Quest", JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.nomeJogador = nome;
            this.emailJogador = email;
            estaLogado = true;
            telaAtual = 0;
            atualizarVisibilidadeCampos();
            repaint();
        } else {
            if (email.isEmpty() || senha.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Digite seu email e senha para entrar!", "Hobby Quest", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!email.contains("@")) {
                this.nomeJogador = email;
            } else {
                this.nomeJogador = email.substring(0, email.indexOf('@'));
            }
            this.emailJogador = email;
            estaLogado = true;
            telaAtual = 0;
            atualizarVisibilidadeCampos();
            repaint();
        }
    }

    // ==========================================
    // EVENTOS DO MOUSE
    // ==========================================
    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        // Overlay de conquistas: qualquer clique fecha
        if (mostrandoConquistas) {
            mostrandoConquistas = false;
            repaint();
            return;
        }

        if (!estaLogado) {
            // Botão de olho para mostrar/ocultar senha
            if (btnOlhoSenha.contains(x, y)) {
                senhaVisivel = !senhaVisivel;
                txtSenha.setEchoChar(senhaVisivel ? (char) 0 : '\u2022');
                repaint();
                return;
            }
            if (modoCadastro && btnOlhoConfirmaSenha.contains(x, y)) {
                confirmaSenhaVisivel = !confirmaSenhaVisivel;
                txtConfirmaSenha.setEchoChar(confirmaSenhaVisivel ? (char) 0 : '\u2022');
                repaint();
                return;
            }
            if (btnPostItToggle.contains(x, y)) {
                iniciarAnimacaoClique(btnPostItToggle);
                modoCadastro = !modoCadastro;
                // Resetar visibilidade das senhas ao trocar modo
                senhaVisivel = false;
                confirmaSenhaVisivel = false;
                txtSenha.setEchoChar('\u2022');
                txtConfirmaSenha.setEchoChar('\u2022');
                atualizarVisibilidadeCampos();
                repaint();
                return;
            }
            if (btnSubmitAuth.contains(x, y)) {
                iniciarAnimacaoClique(btnSubmitAuth);
                realizarLoginOuCadastro();
                return;
            }
            if (!modoCadastro && btnEsqueciSenha.contains(x, y)) {
                JOptionPane.showMessageDialog(this, "Para redefinir sua senha, crie uma nova conta no Post-it!", "Hobby Quest", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            return;
        }

        // Navegação — com transição animada e feedback de clique
        if (telaAtual <= 4) {
            for (int i = 0; i < 5; i++) {
                if (navTabs[i].contains(x, y)) {
                    iniciarAnimacaoClique(navTabs[i]);
                    if (i != telaAtual) {
                        iniciarTransicao(i);
                    }
                    return;
                }
            }
        }

        // Home
        if (telaAtual == 0) {
            if (btnIniciarJornada.contains(x, y)) {
                iniciarAnimacaoClique(btnIniciarJornada);
                // Delay para ver a animação antes de navegar
                Timer t = new Timer(200, ev -> { ((Timer)ev.getSource()).stop(); frame.mostrarMenuPrincipal(); });
                t.setRepeats(false);
                t.start();
                return;
            }
            if (cardArte.contains(x, y)) {
                iniciarAnimacaoClique(cardArte);
                iniciarTransicao(1);
                return;
            }
            if (cardLeitura.contains(x, y)) {
                iniciarAnimacaoClique(cardLeitura);
                iniciarTransicao(1);
                return;
            }
            if (cardFitness.contains(x, y)) {
                iniciarAnimacaoClique(cardFitness);
                iniciarTransicao(1);
                return;
            }
        }

        // Objetivos
        if (telaAtual == 1) {
            // Botão Adicionar Meta (+)
            if (btnAddLeitura.contains(x, y)) {
                iniciarAnimacaoClique(btnAddLeitura);
                adicionarMetaLeitura();
                return;
            }
            if (btnAddArte.contains(x, y)) {
                iniciarAnimacaoClique(btnAddArte);
                adicionarMetaArte();
                return;
            }
            if (btnAddFitness.contains(x, y)) {
                iniciarAnimacaoClique(btnAddFitness);
                adicionarMetaFitness();
                return;
            }

            // Botão Desfazer / Apagar o que fiz anteriormente (↺)
            if (btnRefLeitura.contains(x, y)) {
                iniciarAnimacaoClique(btnRefLeitura);
                desfazerLeitura();
                return;
            }
            if (btnRefArte.contains(x, y)) {
                iniciarAnimacaoClique(btnRefArte);
                desfazerArte();
                return;
            }
            if (btnRefFitness.contains(x, y)) {
                iniciarAnimacaoClique(btnRefFitness);
                desfazerFitness();
                return;
            }

            // Metas de Leitura (Check e Delete)
            for (int i = metasLeitura.size() - 1; i >= 0; i--) {
                MetaItem item = metasLeitura.get(i);
                if (item.checkRect.contains(x, y)) {
                    item.concluida = !item.concluida;
                    if (item.concluida) {
                        totalXP += item.xp;
                        xpLeitura += item.xp;
                        totalDesafios++;
                        animacoesXP.add(new AnimacaoXP("+" + item.xp + " XP", item.checkRect.x + 240, item.checkRect.y));
                    } else {
                        totalXP = Math.max(0, totalXP - item.xp);
                        xpLeitura = Math.max(0, xpLeitura - item.xp);
                        totalDesafios = Math.max(0, totalDesafios - 1);
                    }
                    historicoLeitura.add(new HistoricoAcao(TipoAcaoObjetivo.TOGGLE_CHECK, item, i));
                    repaint();
                    return;
                }
                if (item.deleteRect.contains(x, y)) {
                    if (item.concluida) {
                        totalXP = Math.max(0, totalXP - item.xp);
                        xpLeitura = Math.max(0, xpLeitura - item.xp);
                        totalDesafios = Math.max(0, totalDesafios - 1);
                    }
                    historicoLeitura.add(new HistoricoAcao(TipoAcaoObjetivo.REMOVEU, item, i));
                    metasLeitura.remove(i);
                    repaint();
                    return;
                }
            }

            // Metas de Arte (Check e Delete)
            for (int i = metasArte.size() - 1; i >= 0; i--) {
                MetaItem item = metasArte.get(i);
                if (item.checkRect.contains(x, y)) {
                    item.concluida = !item.concluida;
                    if (item.concluida) {
                        totalXP += item.xp;
                        xpArte += item.xp;
                        totalDesafios++;
                        animacoesXP.add(new AnimacaoXP("+" + item.xp + " XP", item.checkRect.x + 240, item.checkRect.y));
                    } else {
                        totalXP = Math.max(0, totalXP - item.xp);
                        xpArte = Math.max(0, xpArte - item.xp);
                        totalDesafios = Math.max(0, totalDesafios - 1);
                    }
                    historicoArte.add(new HistoricoAcao(TipoAcaoObjetivo.TOGGLE_CHECK, item, i));
                    repaint();
                    return;
                }
                if (item.deleteRect.contains(x, y)) {
                    if (item.concluida) {
                        totalXP = Math.max(0, totalXP - item.xp);
                        xpArte = Math.max(0, xpArte - item.xp);
                        totalDesafios = Math.max(0, totalDesafios - 1);
                    }
                    historicoArte.add(new HistoricoAcao(TipoAcaoObjetivo.REMOVEU, item, i));
                    metasArte.remove(i);
                    repaint();
                    return;
                }
            }

            // Metas de Fitness (Check e Delete)
            for (int i = metasFitness.size() - 1; i >= 0; i--) {
                MetaItem item = metasFitness.get(i);
                if (item.checkRect.contains(x, y)) {
                    item.concluida = !item.concluida;
                    if (item.concluida) {
                        totalXP += item.xp;
                        xpFitness += item.xp;
                        totalDesafios++;
                        animacoesXP.add(new AnimacaoXP("+" + item.xp + " XP", item.checkRect.x + 240, item.checkRect.y));
                    } else {
                        totalXP = Math.max(0, totalXP - item.xp);
                        xpFitness = Math.max(0, xpFitness - item.xp);
                        totalDesafios = Math.max(0, totalDesafios - 1);
                    }
                    historicoFitness.add(new HistoricoAcao(TipoAcaoObjetivo.TOGGLE_CHECK, item, i));
                    repaint();
                    return;
                }
                if (item.deleteRect.contains(x, y)) {
                    if (item.concluida) {
                        totalXP = Math.max(0, totalXP - item.xp);
                        xpFitness = Math.max(0, xpFitness - item.xp);
                        totalDesafios = Math.max(0, totalDesafios - 1);
                    }
                    historicoFitness.add(new HistoricoAcao(TipoAcaoObjetivo.REMOVEU, item, i));
                    metasFitness.remove(i);
                    repaint();
                    return;
                }
            }
        }

        // Perfil
        if (telaAtual == 4) {
            if (btnUploadFoto.contains(x, y)) {
                iniciarAnimacaoClique(btnUploadFoto);
                if (fotoCustomizada != null) {
                    fotoCustomizada = null;
                    avatarLimiteCustom = null;
                    repaint();
                } else {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle("Escolha sua foto de perfil");
                    chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                        "Imagens (JPG, PNG, GIF, BMP)", "jpg", "jpeg", "png", "gif", "bmp"));
                    int result = chooser.showOpenDialog(this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        try {
                            BufferedImage loaded = ImageIO.read(chooser.getSelectedFile());
                            if (loaded != null) {
                                fotoCustomizada = loaded;
                                avatarLimiteCustom = calcularLimitesConteudo(loaded);
                                repaint();
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this,
                                "N\u00e3o foi poss\u00edvel carregar a imagem.", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                return;
            }
            if (btnTrocarNome.contains(x, y)) {
                iniciarAnimacaoClique(btnTrocarNome);
                String novo = JOptionPane.showInputDialog(this, "Digite seu novo nome de aventureiro:", nomeJogador);
                if (novo != null && !novo.trim().isEmpty()) {
                    nomeJogador = novo.trim();
                    repaint();
                }
                return;
            }
            if (btnAbrirConquistas.contains(x, y)) {
                iniciarAnimacaoClique(btnAbrirConquistas);
                mostrandoConquistas = true;
                repaint();
                return;
            }
            if (btnLogout.contains(x, y)) {
                iniciarAnimacaoClique(btnLogout);
                estaLogado = false;
                txtEmail.setText("");
                txtSenha.setText("");
                txtConfirmaSenha.setText("");
                atualizarVisibilidadeCampos();
                repaint();
                return;
            }
            for (int i = 0; i < 7; i++) {
                if (avatarBotoes[i].contains(x, y)) {
                    iniciarAnimacaoClique(avatarBotoes[i]);
                    avatarSelecionado = i;
                    repaint();
                    return;
                }
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (!estaLogado) {
            boolean newHoverSubmit = btnSubmitAuth.contains(x, y);
            boolean newHoverPostIt = btnPostItToggle.contains(x, y);
            boolean newHoverEsqueci = (!modoCadastro && btnEsqueciSenha.contains(x, y));
            boolean newHoverOlho = (btnOlhoSenha.contains(x, y) || (modoCadastro && btnOlhoConfirmaSenha.contains(x, y)));

            hoverSubmit = newHoverSubmit;
            hoverPostIt = newHoverPostIt;
            hoverEsqueci = newHoverEsqueci;
            hoverOlho = newHoverOlho;

            boolean hand = hoverSubmit || hoverPostIt || hoverEsqueci || hoverOlho;
            setCursor(hand ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            return;
        }

        boolean hand = false;

        // Feedback de cursor na tela de Objetivos
        if (telaAtual == 1) {
            if (btnAddLeitura.contains(x, y) || btnRefLeitura.contains(x, y)
                    || btnAddArte.contains(x, y) || btnRefArte.contains(x, y)
                    || btnAddFitness.contains(x, y) || btnRefFitness.contains(x, y)) {
                hand = true;
            } else {
                for (MetaItem m : metasLeitura) {
                    if (m.checkRect.contains(x, y) || m.deleteRect.contains(x, y)) {
                        hand = true;
                        break;
                    }
                }
                if (!hand) {
                    for (MetaItem m : metasArte) {
                        if (m.checkRect.contains(x, y) || m.deleteRect.contains(x, y)) {
                            hand = true;
                            break;
                        }
                    }
                }
                if (!hand) {
                    for (MetaItem m : metasFitness) {
                        if (m.checkRect.contains(x, y) || m.deleteRect.contains(x, y)) {
                            hand = true;
                            break;
                        }
                    }
                }
            }
        }

        boolean needRepaint = false;

        boolean newHoverIniciar = (telaAtual == 0 && btnIniciarJornada.contains(x, y));
        if (newHoverIniciar != hoverIniciar) { hoverIniciar = newHoverIniciar; needRepaint = true; }
        if (newHoverIniciar) hand = true;

        int newHoverCard = -1;
        if (telaAtual == 0) {
            if (cardArte.contains(x, y)) newHoverCard = 0;
            else if (cardLeitura.contains(x, y)) newHoverCard = 1;
            else if (cardFitness.contains(x, y)) newHoverCard = 2;
            if (newHoverCard != -1) hand = true;
        }
        if (newHoverCard != hoverCard) { hoverCard = newHoverCard; needRepaint = true; }

        int newHoverTab = -1;
        if (telaAtual <= 4) {
            for (int i = 0; i < 5; i++) {
                if (navTabs[i].contains(x, y)) { newHoverTab = i; hand = true; break; }
            }
        }
        if (newHoverTab != hoverTab) { hoverTab = newHoverTab; needRepaint = true; }

        setCursor(hand ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());

        if (needRepaint) repaint();
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {
        hoverSubmit = false;
        hoverPostIt = false;
        hoverEsqueci = false;
        hoverOlho = false;
        hoverIniciar = false;
        hoverCard = -1;
        hoverTab = -1;
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        if (telaAtual == 1) {
            int x = e.getX(), y = e.getY();
            if (btnRefLeitura.contains(x, y) || btnRefArte.contains(x, y) || btnRefFitness.contains(x, y)) {
                return "Apagar / Desfazer o que fiz anteriormente";
            }
            if (btnAddLeitura.contains(x, y) || btnAddArte.contains(x, y) || btnAddFitness.contains(x, y)) {
                return "Adicionar meta";
            }
        }
        return super.getToolTipText(e);
    }
}
