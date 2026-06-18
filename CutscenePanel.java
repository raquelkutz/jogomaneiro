import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.io.File;

public class CutscenePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {
    private JogoAudrey frame;
    private final int LARGURA = 1000;
    private final int ALTURA = 750;
    
    private int cenaAtual = 0;
    private int cutsceneAtualId = 0;
    
    private Font fontCrayonHand;
    private Image[] imagens;
    
    // Botões
    private int botaoLargura = 180;
    private int botaoAltura = 50;
    private int botaoProximoX, botaoProximoY;
    private int botaoAnteriorX, botaoAnteriorY;
    private int botaoPularX, botaoPularY;
    private int botaoPularLargura = 130;
    private int botaoPularAltura = 40;

    private void atualizarPosicoesBotoes() {
        int w = getWidth();
        int h = getHeight();
        botaoProximoX = w - botaoLargura - 20;
        botaoProximoY = h - 80;
        botaoAnteriorX = 20;
        botaoAnteriorY = h - 80;
        botaoPularX = w - 150;
        botaoPularY = 20;
    }
    
    private boolean botaoProximoHover = false;
    private boolean botaoAnteriorHover = false;
    private boolean botaoPularHover = false;

    // Transição entre slides
    private boolean emTransicao = false;
    private int progressoTransicao = 0;
    private final int PASSOS_TRANSICAO = 30;
    private int proximaCena = 0;
    private int direcaoTransicao = 1; // 1 = avançar, -1 = voltar
    
    private String[] nomesImagens = new String[0];

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
    
    public void iniciarCutscene(int id) {
        this.cutsceneAtualId = id;
        this.cenaAtual = 0;
        this.emTransicao = false;
        
        if (id == 0) {
            nomesImagens = new String[]{
                "slide1.png", "slide2.png", "slide3.png", "slide4.png",
                "slide5.png", "slide6.png", "slide7.png", "slide8.png",
                "slide9.png", "slide10.png", "slide11.png", "slide12.png"
            };
        } else if (id == 1) {
            nomesImagens = new String[]{"cutscene_sala_1.png", "cutscene_sala_2.png"};
        } else if (id == 2) {
            nomesImagens = new String[]{"cutscene_final_1.png", "cutscene_final_2.png"};
        }
        
        imagens = new Image[nomesImagens.length];
        carregarImagens();
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
        for (int i = 0; i < nomesImagens.length; i++) {
            try {
                File arquivo = new File(nomesImagens[i]);
                if (arquivo.exists()) {
                    imagens[i] = new ImageIcon(nomesImagens[i]).getImage();
                } else {
                    // Tenta em subpasta cutscene
                    arquivo = new File("cutscene/" + nomesImagens[i]);
                    if (arquivo.exists()) {
                        imagens[i] = new ImageIcon("cutscene/" + nomesImagens[i]).getImage();
                    } else {
                        // Placeholder se a imagem não existir
                        imagens[i] = new ImageIcon("placeholder_cutscene.png").getImage();
                    }
                }
            } catch (Exception e) {
                System.err.println("Erro ao carregar imagem: " + nomesImagens[i]);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        atualizarPosicoesBotoes();
        int w = getWidth();
        int h = getHeight();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Fundo bege do notebook com gradiente
        GradientPaint bgGrad = new GradientPaint(0, 0, new Color(248, 243, 228), w, h, new Color(238, 230, 212));
        g2d.setPaint(bgGrad);
        g2d.fillRect(0, 0, w, h);

        // Rabiscos de fundo
        desenharRabiscosFundo(g2d, w, h);

        if (emTransicao) {
            float t = easing((float) progressoTransicao / PASSOS_TRANSICAO);

            desenharSlide(g2d, cenaAtual, 0);

            Shape oldClip = g2d.getClip();
            if (direcaoTransicao == 1) {
                int foldX = (int) (w * (1 - t));
                g2d.setClip(foldX, 0, w - foldX, h);
                desenharSlide(g2d, proximaCena, 0);
                g2d.setClip(oldClip);

                if (t > 0 && t < 1) {
                    GradientPaint shadow = new GradientPaint(foldX, 0, new Color(0, 0, 0, 60), foldX + 15, 0, new Color(0, 0, 0, 0));
                    g2d.setPaint(shadow);
                    g2d.fillRect(foldX, 0, 15, h);
                }
            } else {
                int foldX = (int) (w * t);
                g2d.setClip(0, 0, foldX, h);
                desenharSlide(g2d, proximaCena, 0);
                g2d.setClip(oldClip);

                if (t > 0 && t < 1) {
                    GradientPaint shadow = new GradientPaint(foldX, 0, new Color(0, 0, 0, 0), foldX - 15, 0, new Color(0, 0, 0, 60));
                    g2d.setPaint(shadow);
                    g2d.fillRect(foldX - 15, 0, 15, h);
                }
            }
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            desenharSlide(g2d, cenaAtual, 0);
        }

        desenharBotaoBaixo(g2d);
    }

    /** Desenha o slide com moldura de caderno centralizado */
    private void desenharSlide(Graphics2D g2d, int indice, int offsetX) {
        if (indice < 0 || indice >= imagens.length || imagens[indice] == null) return;
        int w = getWidth();
        int h = getHeight();
        int imgL = imagens[indice].getWidth(this);
        int imgH = imagens[indice].getHeight(this);
        if (imgL <= 0 || imgH <= 0) return;

        // Desenha a imagem preenchendo a tela, cobrindo o painel inteiro
        double scale = Math.max((double)w / imgL, (double)h / imgH);
        int finalW = (int)(imgL * scale);
        int finalH = (int)(imgH * scale);
        int imgX = offsetX + (w - finalW) / 2;
        int imgY = (h - finalH) / 2;
        
        g2d.drawImage(imagens[indice], imgX, imgY, finalW, finalH, this);

        // Fundo escuro atrás dos botões para garantir leitura
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRect(0, h - 100, w, 100);
        g2d.fillRect(w - 170, 10, 160, 60);

        g2d.setStroke(new BasicStroke(2f));
    }

    /** Rabiscos fofos de fundo (estilo caderno) */
    private void desenharRabiscosFundo(Graphics2D g2d, int w, int h) {
        Stroke s = new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(s);

        g2d.setColor(new Color(210, 160, 180, 100));
        cutEstrela4(g2d, (int)(w*0.03), (int)(h*0.07), 7);
        cutEstrela4(g2d, (int)(w*0.96), (int)(h*0.88), 7);
        cutEstrela4(g2d, (int)(w*0.97), (int)(h*0.06), 6);
        cutEstrela4(g2d, (int)(w*0.02), (int)(h*0.90), 6);

        g2d.setColor(new Color(180, 200, 170, 85));
        for (int i = 0; i < 3; i++) {
            g2d.fillOval(10, (int)(h*0.30) + i*22, 5, 5);
            g2d.fillOval(w-16, (int)(h*0.55) + i*22, 5, 5);
        }

        g2d.setColor(new Color(200, 170, 180, 70));
        GeneralPath wave = new GeneralPath();
        wave.moveTo(18, h - 45);
        for (int i = 0; i < 10; i++) {
            wave.quadTo(18 + i*30 + 15, h - 58, 18 + (i+1)*30, h - 45);
        }
        g2d.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(wave);

        g2d.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }

    private void cutEstrela4(Graphics2D g2d, int cx, int cy, int r) {
        int r2 = Math.max(2, r/3);
        g2d.drawLine(cx-r, cy, cx+r, cy);
        g2d.drawLine(cx, cy-r, cx, cy+r);
        g2d.drawLine(cx-r2, cy-r2, cx+r2, cy+r2);
        g2d.drawLine(cx-r2, cy+r2, cx+r2, cy-r2);
    }

    /** Ease-in-out cúbico — mais suave que quadrático */
    private float easing(float t) {
        return t < 0.5f
            ? 4 * t * t * t
            : 1f - (float)Math.pow(-2f * t + 2f, 3) / 2f;
    }

    private void desenharBotaoBaixo(Graphics2D g2d) {
        Color corFundo = new Color(255, 230, 235);
        Color corFundoHover = new Color(255, 210, 220);
        Color corTexto = new Color(120, 80, 100);

        // Botão ANTERIOR
        if (cenaAtual > 0) {
            String textoBotaoAnterior = "◄ ANTERIOR";
            g2d.setColor(botaoAnteriorHover ? corFundoHover : corFundo);
            g2d.fillRoundRect(botaoAnteriorX, botaoAnteriorY, botaoLargura, botaoAltura, botaoAltura, botaoAltura);

            g2d.setFont(fontCrayonHand.deriveFont(16f));
            g2d.setColor(corTexto);
            FontMetrics fm = g2d.getFontMetrics();
            int textX = botaoAnteriorX + (botaoLargura - fm.stringWidth(textoBotaoAnterior)) / 2;
            int textY = botaoAnteriorY + ((botaoAltura - fm.getHeight()) / 2) + fm.getAscent();
            g2d.drawString(textoBotaoAnterior, textX, textY);
        }

        // Botão PRÓXIMO / COMEÇAR
        String textoBotao = (imagens != null && cenaAtual < imagens.length - 1) ? "PRÓXIMO ►" : (cutsceneAtualId == 0 ? "COMEÇAR AVENTURA" : "VOLTAR AO JOGO");
        g2d.setColor(botaoProximoHover ? corFundoHover : corFundo);
        g2d.fillRoundRect(botaoProximoX, botaoProximoY, botaoLargura, botaoAltura, botaoAltura, botaoAltura);

        g2d.setFont(fontCrayonHand.deriveFont(16f));
        g2d.setColor(corTexto);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = botaoProximoX + (botaoLargura - fm.stringWidth(textoBotao)) / 2;
        int textY = botaoProximoY + ((botaoAltura - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(textoBotao, textX, textY);

        // Botão PULAR
        String textoPular = "PULAR ⏭";
        g2d.setColor(botaoPularHover ? corFundoHover : corFundo);
        g2d.fillRoundRect(botaoPularX, botaoPularY, botaoPularLargura, botaoPularAltura, botaoPularAltura, botaoPularAltura);

        g2d.setFont(fontCrayonHand.deriveFont(16f));
        g2d.setColor(corTexto);
        FontMetrics fmPular = g2d.getFontMetrics();
        int textXp = botaoPularX + (botaoPularLargura - fmPular.stringWidth(textoPular)) / 2;
        int textYp = botaoPularY + ((botaoPularAltura - fmPular.getHeight()) / 2) + fmPular.getAscent();
        g2d.drawString(textoPular, textXp, textYp);
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
        if (imagens != null && cenaAtual < imagens.length - 1) {
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
        frame.voltarDeCutscene();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        atualizarPosicoesBotoes();
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

        // Verifica se clicou no botão PULAR
        if (mouseX >= botaoPularX && mouseX <= botaoPularX + botaoPularLargura &&
            mouseY >= botaoPularY && mouseY <= botaoPularY + botaoPularAltura) {
            iniciarJogo();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        atualizarPosicoesBotoes();
        int mouseX = e.getX();
        int mouseY = e.getY();

        // Verifica se está sobre o botão PRÓXIMO
        boolean novoHoverProximo = (mouseX >= botaoProximoX && mouseX <= botaoProximoX + botaoLargura &&
                                    mouseY >= botaoProximoY && mouseY <= botaoProximoY + botaoAltura);
        
        // Verifica se está sobre o botão ANTERIOR
        boolean novoHoverAnterior = (cenaAtual > 0 &&
                                     mouseX >= botaoAnteriorX && mouseX <= botaoAnteriorX + botaoLargura &&
                                     mouseY >= botaoAnteriorY && mouseY <= botaoAnteriorY + botaoAltura);
        
        // Verifica se está sobre o botão PULAR
        boolean novoHoverPular = (mouseX >= botaoPularX && mouseX <= botaoPularX + botaoPularLargura &&
                                  mouseY >= botaoPularY && mouseY <= botaoPularY + botaoPularAltura);
        
        if (novoHoverProximo != botaoProximoHover || novoHoverAnterior != botaoAnteriorHover || novoHoverPular != botaoPularHover) {
            botaoProximoHover = novoHoverProximo;
            botaoAnteriorHover = novoHoverAnterior;
            botaoPularHover = novoHoverPular;
            
            if (novoHoverProximo || novoHoverAnterior || novoHoverPular) {
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
        if (botaoProximoHover || botaoAnteriorHover || botaoPularHover) {
            botaoProximoHover = false;
            botaoAnteriorHover = false;
            botaoPularHover = false;
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
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            int opcao = JOptionPane.showConfirmDialog(this,
                "Deseja voltar ao menu principal?",
                "Menu",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if (opcao == JOptionPane.YES_OPTION) {
                frame.voltarAoMenuPrincipal();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}
