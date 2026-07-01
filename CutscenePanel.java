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
    private int botaoPularX, botaoPularY;
    private int botaoPularLargura = 130;
    private int botaoPularAltura = 40;
    private void atualizarPosicoesBotoes() {
        int w = getWidth();
        botaoPularX = w - 150;
        botaoPularY = 20;
    }
    
    private boolean botaoPularHover = false;

    // Transição entre slides
    private boolean emTransicao = false;
    private int progressoTransicao = 0;
    private final int PASSOS_TRANSICAO = 30;
    private int proximaCena = 0;
    private int direcaoTransicao = 1; // 1 = avançar, -1 = voltar
    
    // Animação do botão "Começar aventura"
    private int pulseFrame = 0;
    private boolean botaoIniciarHover = false;
    
    // Fade transition
    private boolean emFade = false;
    private int fadeProgresso = 0;
    private final int FADE_MAX = 40;
    private boolean fadeAcaoExecutada = false;
    
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

            float scale = 1f - t * 0.03f;
            int offsetX = (int) ((1 - scale) * w / 2);
            g2d.translate(offsetX, 0);
            g2d.scale(scale, 1);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f - t * 0.3f));
            desenharSlide(g2d, cenaAtual, 0);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g2d.scale(1 / scale, 1);
            g2d.translate(-offsetX, 0);

            int slideW = (int) (w * t);
            int slideX = direcaoTransicao == 1 ? w - slideW : 0;
            Shape oldClip = g2d.getClip();
            g2d.setClip(slideX, 0, slideW, h);
            desenharSlide(g2d, proximaCena, 0);
            g2d.setClip(oldClip);

            if (t > 0.01f && t < 0.99f) {
                int shadowX = direcaoTransicao == 1 ? slideX : slideX + slideW;
                int dir = direcaoTransicao == 1 ? 1 : -1;
                GradientPaint shadow = new GradientPaint(
                    shadowX, 0, new Color(0, 0, 0, (int)(80 * t * (1 - t) * 4)),
                    shadowX + dir * 30, 0, new Color(0, 0, 0, 0)
                );
                g2d.setPaint(shadow);
                g2d.fillRect(Math.min(shadowX, shadowX + dir * 30), 0, 30, h);
            }
        } else {
            desenharSlide(g2d, cenaAtual, 0);
        }

        desenharBotaoBaixo(g2d);

        if (emFade) {
            int alpha;
            if (fadeProgresso < FADE_MAX) {
                alpha = (int)(fadeProgresso * 255 / FADE_MAX);
            } else {
                alpha = 255 - (int)((fadeProgresso - FADE_MAX) * 255 / FADE_MAX);
            }
            g2d.setColor(new Color(0, 0, 0, Math.min(255, Math.max(0, alpha))));
            g2d.fillRect(0, 0, w, h);
        }
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

        // Fundo escuro atrás do botão pular
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRect(w - 170, 10, 160, 60);

        // Animação no botão "Começar aventura" (último slide)
        if (indice == imagens.length - 1) {
            double s2 = Math.max((double)w / imgL, (double)h / imgH);
            int fw = (int)(imgL * s2);
            int fh = (int)(imgH * s2);
            int ix = offsetX + (w - fw) / 2;
            int iy = (h - fh) / 2;

            int btnIX = 541, btnIY = 909, btnIW = 786, btnIH = 78;
            int bx = ix + (int)(btnIX * s2);
            int by = iy + (int)(btnIY * s2);
            int bw = (int)(btnIW * s2);
            int bh = (int)(btnIH * s2);
            int arc = (int)(bh * 0.7);

            float t = pulseFrame * 0.06f;
            float p1 = (float) (Math.sin(t) * 0.5 + 0.5);
            float p2 = (float) (Math.sin(t + 1.2) * 0.5 + 0.5);
            float p3 = (float) (Math.sin(t + 2.4) * 0.5 + 0.5);

            // Camada externa - brilho verde suave
            int a1 = (int) (40 + p1 * 80);
            g2d.setColor(new Color(80, 220, 80, a1));
            g2d.setStroke(new BasicStroke(4 + p2 * 6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawRoundRect(bx - 8, by - 8, bw + 16, bh + 16, arc + 4, arc + 4);

            // Camada média - brilho verde-limão
            int a2 = (int) (60 + p2 * 100);
            g2d.setColor(new Color(160, 255, 100, a2));
            g2d.setStroke(new BasicStroke(2 + p3 * 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawRoundRect(bx - 4, by - 4, bw + 8, bh + 8, arc, arc);

            // Camada interna - brilho branco suave
            int a3 = (int) (80 + p1 * 120);
            g2d.setColor(new Color(220, 255, 200, a3));
            g2d.setStroke(new BasicStroke(1 + p2 * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawRoundRect(bx, by, bw, bh, arc, arc);

            // Partículas flutuantes ao redor do botão
            int cx = bx + bw / 2;
            int cy = by + bh / 2;
            int raioX = bw / 2 + 20;
            int raioY = bh / 2 + 12;
            g2d.setStroke(new BasicStroke(1));
            for (int i = 0; i < 6; i++) {
                float ang = (float) (t * 0.8 + i * 1.047);
                float dist = 0.7f + (float) (Math.sin(t * 0.5 + i) * 0.3);
                int px = cx + (int)(Math.cos(ang) * raioX * dist);
                int py = cy + (int)(Math.sin(ang) * raioY * dist);
                int sz = (int) (2 + p3 * 3);
                int pa = (int) (100 + p1 * 120);
                g2d.setColor(new Color(180, 255, 140, pa));
                g2d.fillOval(px - sz / 2, py - sz / 2, sz, sz);
            }
        }

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
        if (emFade) {
            fadeProgresso++;
            if (fadeProgresso >= FADE_MAX && !fadeAcaoExecutada) {
                fadeAcaoExecutada = true;
                iniciarJogo();
            }
            if (fadeProgresso >= FADE_MAX * 2) {
                emFade = false;
                fadeProgresso = 0;
                fadeAcaoExecutada = false;
            }
        }
        pulseFrame++;
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

        if (emTransicao || emFade) return;

        // Verifica se clicou no botão PULAR
        if (mouseX >= botaoPularX && mouseX <= botaoPularX + botaoPularLargura &&
            mouseY >= botaoPularY && mouseY <= botaoPularY + botaoPularAltura) {
            if (!emFade) {
                emFade = true;
                fadeProgresso = 0;
                fadeAcaoExecutada = false;
            }
            return;
        }

        // Último slide: só pode prosseguir clicando no botão "Começar aventura"
        if (imagens != null && cenaAtual == imagens.length - 1) {
            int w = getWidth();
            int h = getHeight();
            int imgL = imagens[cenaAtual].getWidth(this);
            int imgH = imagens[cenaAtual].getHeight(this);
            double s = Math.max((double)w / imgL, (double)h / imgH);
            int imgX = (w - (int)(imgL * s)) / 2;
            int imgY = (h - (int)(imgH * s)) / 2;

            int btnIX = 541, btnIY = 909, btnIW = 786, btnIH = 78;
            int btnX = imgX + (int)(btnIX * s);
            int btnY = imgY + (int)(btnIY * s);
            int btnW = (int)(btnIW * s);
            int btnH = (int)(btnIH * s);
            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                if (!emFade) {
                    emFade = true;
                    fadeProgresso = 0;
                    fadeAcaoExecutada = false;
                }
                return;
            }
            return; // impede navegação por clique no último slide
        }

        int meio = getWidth() / 2;
        if (mouseX > meio) {
            avancarSlide();
        } else if (cenaAtual > 0) {
            voltarSlide();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        atualizarPosicoesBotoes();
        int mouseX = e.getX();
        int mouseY = e.getY();

        boolean novoHoverPular = (mouseX >= botaoPularX && mouseX <= botaoPularX + botaoPularLargura &&
                                  mouseY >= botaoPularY && mouseY <= botaoPularY + botaoPularAltura);

        boolean novoHoverIniciar = false;
        if (imagens != null && cenaAtual == imagens.length - 1) {
            int w = getWidth();
            int h = getHeight();
            int imgL = imagens[cenaAtual].getWidth(this);
            int imgH = imagens[cenaAtual].getHeight(this);
            double s = Math.max((double)w / imgL, (double)h / imgH);
            int imgX = (w - (int)(imgL * s)) / 2;
            int imgY = (h - (int)(imgH * s)) / 2;
            int btnX = imgX + (int)(555 * s);
            int btnY = imgY + (int)(910 * s);
            int btnW = (int)(790 * s);
            int btnH = (int)(78 * s);
            novoHoverIniciar = (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH);
        }

        if (novoHoverPular != botaoPularHover || novoHoverIniciar != botaoIniciarHover) {
            botaoPularHover = novoHoverPular;
            botaoIniciarHover = novoHoverIniciar;
            
            if (novoHoverPular || novoHoverIniciar) {
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
        if (botaoPularHover || botaoIniciarHover) {
            botaoPularHover = false;
            botaoIniciarHover = false;
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
