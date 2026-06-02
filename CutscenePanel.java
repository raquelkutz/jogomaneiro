import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class CutscenePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {
    private JogoAudrey frame;
    private final int LARGURA = 1000;
    private final int ALTURA = 750;
    
    private int cenaAtual = 0;
    
    private Font fontCrayonHand;
    private Image[] imagens = new Image[12];
    
    // Botões
    private int botaoLargura = 180;
    private int botaoAltura = 50;
    private int botaoProximoX = LARGURA - botaoLargura - 20;
    private int botaoProximoY = ALTURA - 80;
    
    private int botaoAnteriorX = 20;
    private int botaoAnteriorY = ALTURA - 80;
    
    private boolean botaoProximoHover = false;
    private boolean botaoAnteriorHover = false;

    // Transição entre slides
    private boolean emTransicao = false;
    private int progressoTransicao = 0;
    private final int PASSOS_TRANSICAO = 30;
    private int proximaCena = 0;
    private int direcaoTransicao = 1; // 1 = avançar, -1 = voltar
    
    private final String[] NOMES_IMAGENS = {
        "slide1.png", "slide2.png", "slide3.png", "slide4.png",
        "slide5.png", "slide6.png", "slide7.png", "slide8.png",
        "slide9.png", "slide10.png", "slide11.png", "slide12.png"
    };

    public CutscenePanel(JogoAudrey frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        setDoubleBuffered(true);
        
        carregarFonts();
        carregarImagens();
        new Timer(16, this).start();
    }

    private void carregarFonts() {
        try {
            fontCrayonHand = Font.createFont(Font.TRUETYPE_FONT, new java.io.File("CrayonHandRegular2016Demo.ttf")).deriveFont(28f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontCrayonHand);
        } catch (Exception e) {
            fontCrayonHand = new Font("Arial", Font.BOLD, 28);
        }
    }

    private void carregarImagens() {
        for (int i = 0; i < NOMES_IMAGENS.length; i++) {
            try {
                File arquivo = new File(NOMES_IMAGENS[i]);
                if (arquivo.exists()) {
                    imagens[i] = new ImageIcon(NOMES_IMAGENS[i]).getImage();
                } else {
                    // Tenta em subpasta cutscene
                    arquivo = new File("cutscene/" + NOMES_IMAGENS[i]);
                    if (arquivo.exists()) {
                        imagens[i] = new ImageIcon("cutscene/" + NOMES_IMAGENS[i]).getImage();
                    }
                }
            } catch (Exception e) {
                System.err.println("Erro ao carregar imagem: " + NOMES_IMAGENS[i]);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Fundo bege do notebook
        g2d.setColor(new Color(240, 235, 220));
        g2d.fillRect(0, 0, LARGURA, ALTURA);

        Shape clipOriginal = g2d.getClip();

        if (emTransicao) {
            float t = easing((float) progressoTransicao / PASSOS_TRANSICAO);

            int offsetVelha = (direcaoTransicao == 1) ? (int)(-LARGURA * 0.3f * t) : (int)(LARGURA * 0.3f * t);
            int offsetNova = (direcaoTransicao == 1) ? (int)(LARGURA * 0.3f * (1 - t)) : (int)(-LARGURA * 0.3f * (1 - t));
            
            float alphaNova = Math.max(0f, Math.min(1f, t));
            float alphaVelha = Math.max(0f, Math.min(1f, 1.0f - t));
            
            // Desenha a cena antiga saindo e apagando
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaVelha));
            desenharSlide(g2d, cenaAtual, offsetVelha);
            
            // Desenha a nova cena entrando e surgindo
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaNova));
            desenharSlide(g2d, proximaCena, offsetNova);
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            desenharSlide(g2d, cenaAtual, 0);
        }

        desenharBotaoBaixo(g2d);
    }

    /** Desenha o slide centralizado com offsetX horizontal */
    private void desenharSlide(Graphics2D g2d, int indice, int offsetX) {
        if (indice < 0 || indice >= imagens.length || imagens[indice] == null) return;
        int imgWidth  = imagens[indice].getWidth(this);
        int imgHeight = imagens[indice].getHeight(this);
        if (imgWidth <= 0 || imgHeight <= 0) return;
        double escala = Math.min((double) LARGURA / imgWidth, (double) ALTURA / imgHeight);
        int nw = (int)(imgWidth  * escala);
        int nh = (int)(imgHeight * escala);
        int x = offsetX + (LARGURA - nw) / 2;
        int y = (ALTURA - nh) / 2;
        g2d.drawImage(imagens[indice], x, y, nw, nh, this);
    }

    // drawFoldShadow foi removido pois a nova animação usa AlphaComposite

    /** Ease-in-out cúbico — mais suave que quadrático */
    private float easing(float t) {
        return t < 0.5f
            ? 4 * t * t * t
            : 1f - (float)Math.pow(-2f * t + 2f, 3) / 2f;
    }

    private void desenharBotaoBaixo(Graphics2D g2d) {
        // Botão ANTERIOR
        if (cenaAtual > 0) {
            String textoBotaoAnterior = "◄ ANTERIOR";
            Color corBotaoAnterior = botaoAnteriorHover ? new Color(150, 100, 100) : new Color(120, 80, 80);
            Color corBorda = new Color(100, 60, 60);

            g2d.setColor(corBotaoAnterior);
            g2d.fillRoundRect(botaoAnteriorX, botaoAnteriorY, botaoLargura, botaoAltura, 10, 10);

            g2d.setColor(corBorda);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(botaoAnteriorX, botaoAnteriorY, botaoLargura, botaoAltura, 10, 10);

            g2d.setFont(fontCrayonHand.deriveFont(16f));
            g2d.setColor(new Color(255, 255, 255));
            FontMetrics fm = g2d.getFontMetrics();
            int textX = botaoAnteriorX + (botaoLargura - fm.stringWidth(textoBotaoAnterior)) / 2;
            int textY = botaoAnteriorY + ((botaoAltura - fm.getHeight()) / 2) + fm.getAscent();
            g2d.drawString(textoBotaoAnterior, textX, textY);
        }

        // Botão PRÓXIMO / COMEÇAR
        String textoBotao = cenaAtual < NOMES_IMAGENS.length - 1 ? "PRÓXIMO ►" : "COMEÇAR AVENTURA";
        Color corBotao = botaoProximoHover ? new Color(100, 150, 100) : new Color(80, 120, 80);
        Color corBorda = new Color(60, 100, 60);

        g2d.setColor(corBotao);
        g2d.fillRoundRect(botaoProximoX, botaoProximoY, botaoLargura, botaoAltura, 10, 10);

        g2d.setColor(corBorda);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(botaoProximoX, botaoProximoY, botaoLargura, botaoAltura, 10, 10);

        g2d.setFont(fontCrayonHand.deriveFont(16f));
        g2d.setColor(new Color(255, 255, 255));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = botaoProximoX + (botaoLargura - fm.stringWidth(textoBotao)) / 2;
        int textY = botaoProximoY + ((botaoAltura - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(textoBotao, textX, textY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (emTransicao) {
            progressoTransicao++;
            if (progressoTransicao >= PASSOS_TRANSICAO) {
                cenaAtual = proximaCena;
                emTransicao = false;
                progressoTransicao = 0;
            }
        }
        repaint();
    }

    private void avancarSlide() {
        if (emTransicao) return;
        if (cenaAtual < NOMES_IMAGENS.length - 1) {
            GerenciadorAudio.tocarSomDialogo();
            proximaCena = cenaAtual + 1;
            direcaoTransicao = 1;
            progressoTransicao = 0;
            emTransicao = true;
        } else {
            iniciarJogo();
        }
    }

    private void voltarSlide() {
        if (emTransicao) return;
        if (cenaAtual > 0) {
            GerenciadorAudio.tocarSomDialogo();
            proximaCena = cenaAtual - 1;
            direcaoTransicao = -1;
            progressoTransicao = 0;
            emTransicao = true;
        }
    }

    private void iniciarJogo() {
        GerenciadorAudio.tocarSomPlay();
        frame.irParaJogoAposCutscene();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        if (emTransicao) return;

        // Verifica se clicou no botão PRÓXIMO
        if (mouseX >= botaoProximoX && mouseX <= botaoProximoX + botaoLargura &&
            mouseY >= botaoProximoY && mouseY <= botaoProximoY + botaoAltura) {
            avancarSlide();
        }

        // Verifica se clicou no botão ANTERIOR
        if (cenaAtual > 0 &&
            mouseX >= botaoAnteriorX && mouseX <= botaoAnteriorX + botaoLargura &&
            mouseY >= botaoAnteriorY && mouseY <= botaoAnteriorY + botaoAltura) {
            voltarSlide();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        // Verifica se está sobre o botão PRÓXIMO
        boolean novoHoverProximo = (mouseX >= botaoProximoX && mouseX <= botaoProximoX + botaoLargura &&
                                    mouseY >= botaoProximoY && mouseY <= botaoProximoY + botaoAltura);
        
        // Verifica se está sobre o botão ANTERIOR
        boolean novoHoverAnterior = (cenaAtual > 0 &&
                                     mouseX >= botaoAnteriorX && mouseX <= botaoAnteriorX + botaoLargura &&
                                     mouseY >= botaoAnteriorY && mouseY <= botaoAnteriorY + botaoAltura);
        
        if (novoHoverProximo != botaoProximoHover || novoHoverAnterior != botaoAnteriorHover) {
            botaoProximoHover = novoHoverProximo;
            botaoAnteriorHover = novoHoverAnterior;
            
            if (novoHoverProximo || novoHoverAnterior) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {
        if (botaoProximoHover || botaoAnteriorHover) {
            botaoProximoHover = false;
            botaoAnteriorHover = false;
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            avancarSlide();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}
