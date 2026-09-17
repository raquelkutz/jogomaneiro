import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;

public class Game extends Canvas implements Runnable {
    
    private static final long serialVersionUID = 1L;
    private Thread thread;
    private boolean running;
    private BufferedImage image;
    
    public JogoAudrey parentFrame;
    public void closeGame() {
        running = false;
        if (parentFrame != null) {
            parentFrame.voltarDoEasterEgg();
        }
    }
    public int[] pixels;
    public Screen screen;
    public Camera camera;
    
    public Texture texture;
    public Texture monsterTexture;
    public Texture coinTexture;
    public Texture portalTexture;
    
    public List<Entity> entities = new ArrayList<>();
    public Entity monster;
    
    public double health = 100;
    public double stamina = 100;
    public int robux = 0;
    public boolean won = false;
    
    public boolean inCutscene = true;
    public long cutsceneStartTime = 0;
    public java.awt.image.BufferedImage cutsceneImg;
    
    private Robot robot;
    private int centerX, centerY;
    private boolean paused = false;
    private boolean lastEsc = false;
    
    private double startX = 7.5, startY = 7.5;
    private List<Entity> toAddEntities = new ArrayList<>();
    private boolean portalSpawned = false;

    // === CONTROLES TOUCH ===
    private boolean touchForward = false, touchBack = false, touchLeft = false, touchRight = false;
    private boolean touchRun = false;
    private int touchDragLastX = -1;
    private boolean isDraggingCamera = false;
    // Retângulos dos botões virtuais (calculados no render)
    private Rectangle btnTouchUp, btnTouchDown, btnTouchLeft2, btnTouchRight2, btnTouchSprint;

    public Game() {
        thread = new Thread(this);
        image = new BufferedImage(960, 720, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
        camera = new Camera(startX, startY, 1, 0, 0, -0.60);
        
        try {
            String path = JogoAudrey.resolvePath("1000132930.jpg");
            if (path == null || !new java.io.File(path).exists()) {
                path = JogoAudrey.resolvePath("../Nova pasta (2)/1000132930.jpg");
            }
            if (path != null && new java.io.File(path).exists()) {
                cutsceneImg = javax.imageio.ImageIO.read(new java.io.File(path));
            }
        } catch (Exception e) {
            System.out.println("Aviso: Imagem 1000132930.jpg não encontrada.");
        }
        
        texture = new Texture(JogoAudrey.resolvePath("../Nova pasta (2)/wall.png"), 128);
        monsterTexture = new Texture(JogoAudrey.resolvePath("../Nova pasta (2)/1000132829-removebg-preview.png"), 512);
        coinTexture = new Texture(JogoAudrey.resolvePath("../Nova pasta (2)/Design sem nome.png"), 256);
        portalTexture = new Texture(JogoAudrey.resolvePath("../Nova pasta (2)/portal.png"), 128);
        
        screen = new Screen(Map.map, 960, 720, texture);
        
        setupEntities();
        
        try {
            robot = new Robot();
        } catch (Exception e) { e.printStackTrace(); }
        
        BufferedImage blankCursor = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor invisibleCursor = Toolkit.getDefaultToolkit().createCustomCursor(blankCursor, new Point(0, 0), "invisible");
        setCursor(invisibleCursor);
        
        addKeyListener(camera);
        addMouseMotionListener(camera);
        
        setBackground(Color.black);
        
        // As posições do mouse serão calculadas no render() quando o componente já estiver visível na tela
        centerX = 0;
        centerY = 0;
        
        // Controles touch - MouseListener para botões virtuais
        addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleTouchPress(e.getX(), e.getY(), true);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                // Soltar todos os botões touch
                touchForward = false;
                touchBack = false;
                touchLeft = false;
                touchRight = false;
                touchRun = false;
                isDraggingCamera = false;
                touchDragLastX = -1;
                syncTouchToCamera();
            }
            @Override public void mouseClicked(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {
                touchForward = false; touchBack = false; touchLeft = false; touchRight = false;
                touchRun = false; isDraggingCamera = false; touchDragLastX = -1;
                syncTouchToCamera();
            }
        });
        
        // Controles touch - drag para girar câmera
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleTouchDrag(e.getX(), e.getY());
            }
            @Override
            public void mouseMoved(MouseEvent e) {}
        });
        
        start();
    }
    
    private void handleTouchPress(int mx, int my, boolean pressed) {
        if (paused || health <= 0 || won || inCutscene) return;
        
        // Verificar se está em algum botão virtual
        if (btnTouchUp != null && btnTouchUp.contains(mx, my)) {
            touchForward = pressed;
        } else if (btnTouchDown != null && btnTouchDown.contains(mx, my)) {
            touchBack = pressed;
        } else if (btnTouchLeft2 != null && btnTouchLeft2.contains(mx, my)) {
            touchLeft = pressed;
        } else if (btnTouchRight2 != null && btnTouchRight2.contains(mx, my)) {
            touchRight = pressed;
        } else if (btnTouchSprint != null && btnTouchSprint.contains(mx, my)) {
            touchRun = pressed;
        } else {
            // Toque fora dos botões = início do drag para girar
            isDraggingCamera = pressed;
            touchDragLastX = mx;
        }
        syncTouchToCamera();
    }
    
    private void handleTouchDrag(int mx, int my) {
        if (paused || health <= 0 || won || inCutscene) return;
        
        // Se está arrastando na área de um botão, verificar e atualizar
        if (btnTouchUp != null && btnTouchUp.contains(mx, my)) {
            touchForward = true; touchBack = false; touchLeft = false; touchRight = false;
            syncTouchToCamera(); return;
        } else if (btnTouchDown != null && btnTouchDown.contains(mx, my)) {
            touchBack = true; touchForward = false; touchLeft = false; touchRight = false;
            syncTouchToCamera(); return;
        } else if (btnTouchLeft2 != null && btnTouchLeft2.contains(mx, my)) {
            touchLeft = true; touchRight = false; touchForward = false; touchBack = false;
            syncTouchToCamera(); return;
        } else if (btnTouchRight2 != null && btnTouchRight2.contains(mx, my)) {
            touchRight = true; touchLeft = false; touchForward = false; touchBack = false;
            syncTouchToCamera(); return;
        } else if (btnTouchSprint != null && btnTouchSprint.contains(mx, my)) {
            touchRun = true; syncTouchToCamera(); return;
        }
        
        // Drag livre para girar câmera
        if (isDraggingCamera && touchDragLastX >= 0) {
            int dx = mx - touchDragLastX;
            if (dx != 0) {
                camera.rotate(-dx * 0.004);
            }
        }
        touchDragLastX = mx;
    }
    
    private void syncTouchToCamera() {
        // Combinar controles touch com teclado (OR lógico)
        // Os controles de teclado já são gerenciados pelo KeyListener da Camera
        // Aqui fazemos override apenas se touch está ativo
        if (touchForward) camera.forward = true;
        if (touchBack) camera.back = true;
        if (touchLeft) camera.left = true;
        if (touchRight) camera.right = true;
        if (touchRun) camera.run = true;
    }
    
    private void setupEntities() {
        entities.clear();
        toAddEntities.clear();
        portalSpawned = false;
        
        // Monstro
        monster = new Entity(14.5, 14.5, monsterTexture, 0);
        entities.add(monster);
        
        // 50 Moedas (Robux) espalhadas pelo mapa com distância mínima entre elas
        int count = 0;
        int maxAttempts = 5000;
        int attempts = 0;
        while (count < 50 && attempts < maxAttempts) {
            attempts++;
            int mx = 2 + (int)(Math.random() * (Map.MAP_WIDTH - 4));
            int my = 2 + (int)(Math.random() * (Map.MAP_HEIGHT - 4));
            if (Map.isWalkable(mx, my) && Map.map[my][mx] != 7 && (mx != (int)startX || my != (int)startY)) {
                boolean tooClose = false;
                for (Entity e : entities) {
                    if (e.type == 1) {
                        double dx = e.x - (mx + 0.5);
                        double dy = e.y - (my + 0.5);
                        if (Math.sqrt(dx*dx + dy*dy) < 3.5) { tooClose = true; break; }
                    }
                }
                if (!tooClose) {
                    entities.add(new Entity(mx + 0.5, my + 0.5, coinTexture, 1));
                    count++;
                }
            }
        }
    }
    
    private void teleportMonsterCloser() {
        int maxDist = 8;
        int minDist = 4;
        for (int attempt = 0; attempt < 50; attempt++) {
            int angle = (int)(Math.random() * 360);
            double dist = minDist + Math.random() * (maxDist - minDist);
            int targetX = (int)(camera.xPos + Math.cos(Math.toRadians(angle)) * dist);
            int targetY = (int)(camera.yPos + Math.sin(Math.toRadians(angle)) * dist);
            
            if (targetX >= 0 && targetX < Map.MAP_WIDTH && targetY >= 0 && targetY < Map.MAP_HEIGHT) {
                if (Map.isWalkable(targetX, targetY)) {
                    monster.x = targetX + 0.5;
                    monster.y = targetY + 0.5;
                    return;
                }
            }
        }
    }
    
    public synchronized void start() {
        running = true;
        thread.start();
    }
    
    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void render() {
        if (!isDisplayable()) return;
        BufferStrategy bs = getBufferStrategy();
        if(bs == null) { createBufferStrategy(3); return; }
        Graphics g = bs.getDrawGraphics();
        
        long elapsedCutscene = 0;
        if (cutsceneStartTime > 0) elapsedCutscene = System.currentTimeMillis() - cutsceneStartTime;
        
        if (inCutscene && elapsedCutscene < 15000) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Fundo preto sombrio
            g2.setColor(new Color(6, 6, 10));
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            int cw = getWidth() / 2;
            int ch = getHeight() / 2;
            
            if (elapsedCutscene < 4000) {
                float alpha = 0;
                if (elapsedCutscene < 1500) alpha = (float)(elapsedCutscene / 1500.0);
                else if (elapsedCutscene > 3000) alpha = (float)((4000 - elapsedCutscene) / 1000.0);
                else alpha = 1.0f;
                if (alpha < 0) alpha = 0; if (alpha > 1) alpha = 1;
                
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, Math.max(22f, (float)(getWidth() / 24.0))));
                FontMetrics fm = g2.getFontMetrics();
                String text = "Você não deveria estar aqui...";
                
                // Sombra do texto
                g2.setColor(new Color(0f, 0f, 0f, alpha * 0.8f));
                g2.drawString(text, cw - fm.stringWidth(text) / 2 + 2, ch + 2);
                // Texto principal
                g2.setColor(new Color(1f, 1f, 1f, alpha));
                g2.drawString(text, cw - fm.stringWidth(text) / 2, ch);
            } else if (elapsedCutscene < 8000) {
                long e = elapsedCutscene - 4000;
                float alpha = 0;
                if (e < 1500) alpha = (float)(e / 1500.0);
                else if (e > 3000) alpha = (float)((4000 - e) / 1000.0);
                else alpha = 1.0f;
                if (alpha < 0) alpha = 0; if (alpha > 1) alpha = 1;
                
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, Math.max(34f, (float)(getWidth() / 16.0))));
                FontMetrics fm = g2.getFontMetrics();
                String text = "CUIDADO";
                
                // Sombra / Brilho vermelho escuro
                g2.setColor(new Color(0.4f, 0f, 0f, alpha * 0.9f));
                g2.drawString(text, cw - fm.stringWidth(text) / 2 + 3, ch - 10 + 3);
                // Texto principal
                g2.setColor(new Color(1f, 0.15f, 0.15f, alpha));
                g2.drawString(text, cw - fm.stringWidth(text) / 2, ch - 10);
                
                // Subtítulo
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, Math.max(16f, (float)(getWidth() / 36.0))));
                FontMetrics fmSub = g2.getFontMetrics();
                String sub = "Algo está te observando nas sombras...";
                g2.setColor(new Color(0.85f, 0.6f, 0.6f, alpha * 0.85f));
                g2.drawString(sub, cw - fmSub.stringWidth(sub) / 2, ch + 35);
            } else {
                long e = elapsedCutscene - 8000;
                float alpha = 0;
                if (e < 1000) alpha = (float)(e / 1000.0);
                else if (e > 5000) alpha = (float)((7000 - e) / 2000.0);
                else alpha = 1.0f;
                if (alpha < 0) alpha = 0; if (alpha > 1) alpha = 1;
                
                if (cutsceneImg != null) {
                    g2.drawImage(cutsceneImg, 0, 0, getWidth(), getHeight(), null);
                    g2.setColor(new Color(0, 0, 0, (int)(120 * alpha)));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, Math.max(26f, (float)(getWidth() / 20.0))));
                FontMetrics fm = g2.getFontMetrics();
                String text = "ELE ESTÁ CHEGANDO...";
                g2.setColor(new Color(0f, 0f, 0f, alpha * 0.9f));
                g2.drawString(text, cw - fm.stringWidth(text) / 2 + 2, ch + 2);
                g2.setColor(new Color(1f, 0.2f, 0.2f, alpha));
                g2.drawString(text, cw - fm.stringWidth(text) / 2, ch);
            }
            
            bs.show();
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int cw = getWidth() / 2;
        int ch = getHeight() / 2;

        // === BARRA DE VIDA ===
        int barW = 180, barH = 14, barX = 18, barY = 18;
        // fundo escuro translúcido
        g2d.setColor(new Color(0, 0, 0, 140));
        g2d.fillRoundRect(barX - 2, barY - 2, barW + 4, barH + 4, 8, 8);
        // barra de fundo (vazia)
        g2d.setColor(new Color(80, 0, 0, 200));
        g2d.fillRoundRect(barX, barY, barW, barH, 6, 6);
        // barra de vida
        int hpW = (int)(Math.max(0, health) / 100.0 * barW);
        if (hpW > 0) {
            float hpFrac = (float)(health / 100.0);
            int hpR = (int)(255 * (1 - hpFrac));
            int hpG = (int)(200 * hpFrac);
            g2d.setColor(new Color(Math.min(255, hpR + 40), Math.min(200, hpG), 30));
            g2d.fillRoundRect(barX, barY, hpW, barH, 6, 6);
            // brilho superior
            g2d.setColor(new Color(255, 255, 255, 60));
            g2d.fillRoundRect(barX, barY, hpW, barH / 2, 6, 6);
        }
        g2d.setColor(new Color(255, 255, 255, 120));
        g2d.setStroke(new java.awt.BasicStroke(1f));
        g2d.drawRoundRect(barX, barY, barW, barH, 6, 6);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2d.setColor(new Color(255, 230, 230));
        g2d.drawString("VIDA", barX + barW + 6, barY + 11);

        // === BARRA DE STAMINA ===
        int stY = barY + barH + 6;
        g2d.setColor(new Color(0, 0, 0, 140));
        g2d.fillRoundRect(barX - 2, stY - 2, barW + 4, 10 + 4, 6, 6);
        g2d.setColor(new Color(0, 40, 80, 200));
        g2d.fillRoundRect(barX, stY, barW, 10, 4, 4);
        int stW = (int)(Math.max(0, stamina) / 100.0 * barW);
        if (stW > 0) {
            g2d.setColor(new Color(30, 180, 255));
            g2d.fillRoundRect(barX, stY, stW, 10, 4, 4);
            g2d.setColor(new Color(255, 255, 255, 55));
            g2d.fillRoundRect(barX, stY, stW, 5, 4, 4);
        }
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.drawRoundRect(barX, stY, barW, 10, 4, 4);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2d.setColor(new Color(180, 230, 255));
        g2d.drawString("STAMINA", barX + barW + 6, stY + 9);

        // === CONTADOR DE ROBUX (canto superior direito) ===
        String robuxTxt = "◈  " + robux + " / 50 Robux";
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics fmR = g2d.getFontMetrics();
        int rxW = fmR.stringWidth(robuxTxt) + 24;
        int rxX = getWidth() - rxW - 14;
        int rxY = 12;
        // fundo pill
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRoundRect(rxX - 4, rxY, rxW, 28, 14, 14);
        g2d.setColor(new Color(255, 210, 0, 60));
        g2d.setStroke(new java.awt.BasicStroke(1.5f));
        g2d.drawRoundRect(rxX - 4, rxY, rxW, 28, 14, 14);
        // sombra do texto
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.drawString(robuxTxt, rxX + 9, rxY + 20);
        g2d.setColor(new Color(255, 210, 0));
        g2d.drawString(robuxTxt, rxX + 8, rxY + 19);
        if (robux >= 50 && !won) {
            g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
            String ptxt = "▶  PORTAL ABERTO!";
            int pw = g2d.getFontMetrics().stringWidth(ptxt) + 18;
            g2d.setColor(new Color(0, 200, 80, 180));
            g2d.fillRoundRect(rxX - 4, rxY + 34, pw, 22, 10, 10);
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.drawString(ptxt, rxX + 9, rxY + 49);
            g2d.setColor(new Color(100, 255, 140));
            g2d.drawString(ptxt, rxX + 8, rxY + 48);
        }

        // === INDICADOR DE DIREÇÃO ===
        renderDirectionIndicator(g, cw);

        // === MIRA CENTRAL ===
        g2d.setStroke(new java.awt.BasicStroke(1.5f));
        // glow exterior
        g2d.setColor(new Color(255, 255, 255, 40));
        g2d.drawLine(cw - 8, ch, cw + 8, ch);
        g2d.drawLine(cw, ch - 8, cw, ch + 8);
        // mira principal
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.drawLine(cw - 5, ch, cw + 5, ch);
        g2d.drawLine(cw, ch - 5, cw, ch + 5);
        // ponto central
        g2d.setColor(new Color(255, 255, 255, 255));
        g2d.fillOval(cw - 1, ch - 1, 3, 3);
        g2d.setStroke(new java.awt.BasicStroke(1f));

        // === TELA DE VITÓRIA ===
        if (won) {
            g2d.setColor(new Color(0, 20, 0, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            // Vinheta verde
            for (int i = 0; i < 5; i++) {
                g2d.setColor(new Color(0, 120 + i*15, 30, 18));
                g2d.fillRect(i*2, i*2, getWidth()-i*4, getHeight()-i*4);
            }
            g2d.setFont(new Font("SansSerif", Font.BOLD, 42));
            String wTxt = "✦  VOCÊ ESCAPOU!  ✦";
            int ww = g2d.getFontMetrics().stringWidth(wTxt);
            // sombra
            g2d.setColor(new Color(0, 80, 0, 255));
            g2d.drawString(wTxt, cw - ww/2 + 3, ch - 20 + 3);
            // texto principal
            g2d.setColor(new Color(120, 255, 160));
            g2d.drawString(wTxt, cw - ww/2, ch - 20);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
            String wSub = "Pressione Q para sair";
            int wsw = g2d.getFontMetrics().stringWidth(wSub);
            g2d.setColor(new Color(180, 255, 200, 200));
            g2d.drawString(wSub, cw - wsw/2, ch + 22);

        // === TELA DE MORTE ===
        } else if (health <= 0) {
            g2d.setColor(new Color(100, 0, 0, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            // vinheta vermelha nas bordas
            for (int i = 0; i < 8; i++) {
                g2d.setColor(new Color(180, 0, 0, 22 - i*2));
                g2d.drawRect(i, i, getWidth()-i*2-1, getHeight()-i*2-1);
            }
            g2d.setFont(new Font("SansSerif", Font.BOLD, 48));
            String dTxt = "VOCÊ FOI MOGGADO";
            int dw = g2d.getFontMetrics().stringWidth(dTxt);
            g2d.setColor(new Color(40, 0, 0, 255));
            g2d.drawString(dTxt, cw - dw/2 + 4, ch - 25 + 4);
            g2d.setColor(new Color(255, 80, 80));
            g2d.drawString(dTxt, cw - dw/2, ch - 25);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 18));
            String dSub = "o monstro te pegou...";
            int dsw = g2d.getFontMetrics().stringWidth(dSub);
            g2d.setColor(new Color(220, 150, 150, 220));
            g2d.drawString(dSub, cw - dsw/2, ch + 18);
            g2d.setFont(new Font("SansSerif", Font.ITALIC, 13));
            String dInst = "[ R ] Tentar novamente";
            int diw = g2d.getFontMetrics().stringWidth(dInst);
            g2d.setColor(new Color(200, 200, 200, 180));
            g2d.drawString(dInst, cw - diw/2, ch + 55);
        }

        // === MENU DE PAUSA ===
        if (paused && health > 0 && !won) {
            g2d.setColor(new Color(0, 0, 0, 190));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            // painel central
            int pw2 = 320, ph2 = 160;
            int px = cw - pw2/2, py = ch - ph2/2;
            g2d.setColor(new Color(20, 20, 30, 230));
            g2d.fillRoundRect(px, py, pw2, ph2, 20, 20);
            g2d.setColor(new Color(255, 255, 255, 50));
            g2d.setStroke(new java.awt.BasicStroke(1.5f));
            g2d.drawRoundRect(px, py, pw2, ph2, 20, 20);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 30));
            String pTxt = "PAUSADO";
            int ptw = g2d.getFontMetrics().stringWidth(pTxt);
            g2d.setColor(new Color(255, 255, 255));
            g2d.drawString(pTxt, cw - ptw/2, py + 52);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
            String p1 = "[ ESC ]  Continuar";
            String p2 = "[ Q ]  Sair do jogo";
            int p1w = g2d.getFontMetrics().stringWidth(p1);
            int p2w = g2d.getFontMetrics().stringWidth(p2);
            g2d.setColor(new Color(200, 200, 255));
            g2d.drawString(p1, cw - p1w/2, py + 95);
            g2d.setColor(new Color(255, 150, 150));
            g2d.drawString(p2, cw - p2w/2, py + 125);
        }

        // === EFEITO DE QUEDA NO BURACO ===
        if (holeEffectAlpha > 0.01f) {
            g2d.setColor(new Color(120, 0, 0, (int)(holeEffectAlpha * 200)));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setFont(new Font("SansSerif", Font.BOLD, Math.max(22, getWidth() / 22)));
            String fallTxt = "⚠  VOCÊ CAIU NO ABISMO!";
            int fw = g2d.getFontMetrics().stringWidth(fallTxt);
            g2d.setColor(new Color(0, 0, 0, (int)(holeEffectAlpha * 200)));
            g2d.drawString(fallTxt, (getWidth() - fw) / 2 + 2, ch + 2);
            g2d.setColor(new Color(255, 255, 255, (int)(holeEffectAlpha * 255)));
            g2d.drawString(fallTxt, (getWidth() - fw) / 2, ch);
            holeEffectAlpha *= 0.88f;
        }

        // === CONTROLES TOUCH VIRTUAIS ===
        if (health > 0 && !won && !paused && !inCutscene) {
            renderTouchControls(g2d);
        }

        bs.show();

    }
    
    private void renderTouchControls(Graphics2D g2d) {
        int W = getWidth(), H = getHeight();
        
        // === D-PAD (canto inferior esquerdo) ===
        int dpadCenterX = 120;
        int dpadCenterY = H - 140;
        int btnSize = 55;
        int gap = 4;
        
        // Calcular retângulos dos botões
        btnTouchUp = new Rectangle(dpadCenterX - btnSize/2, dpadCenterY - btnSize - gap, btnSize, btnSize);
        btnTouchDown = new Rectangle(dpadCenterX - btnSize/2, dpadCenterY + gap, btnSize, btnSize);
        btnTouchLeft2 = new Rectangle(dpadCenterX - btnSize - gap, dpadCenterY - btnSize/2, btnSize, btnSize);
        btnTouchRight2 = new Rectangle(dpadCenterX + gap, dpadCenterY - btnSize/2, btnSize, btnSize);
        
        // Centro do D-pad (decorativo)
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
        g2d.setColor(Color.WHITE);
        g2d.fillOval(dpadCenterX - 18, dpadCenterY - 18, 36, 36);
        
        // Desenhar cada botão do D-pad
        drawTouchButton(g2d, btnTouchUp, "▲", touchForward, new Color(80, 180, 255));
        drawTouchButton(g2d, btnTouchDown, "▼", touchBack, new Color(80, 180, 255));
        drawTouchButton(g2d, btnTouchLeft2, "◄", touchLeft, new Color(80, 180, 255));
        drawTouchButton(g2d, btnTouchRight2, "►", touchRight, new Color(80, 180, 255));
        
        // Label "MOVER"
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2d.setColor(new Color(200, 220, 255));
        String moveLabel = "MOVER";
        FontMetrics fmMove = g2d.getFontMetrics();
        g2d.drawString(moveLabel, dpadCenterX - fmMove.stringWidth(moveLabel)/2, dpadCenterY + btnSize + gap + 22);
        
        // === BOTÃO SPRINT (canto inferior direito) ===
        int sprintSize = 70;
        int sprintX = W - 130;
        int sprintY = H - 160;
        btnTouchSprint = new Rectangle(sprintX - sprintSize/2, sprintY - sprintSize/2, sprintSize, sprintSize);
        
        drawTouchButtonCircle(g2d, sprintX, sprintY, sprintSize, "⚡", touchRun, new Color(255, 200, 50));
        
        // Label "CORRER"
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2d.setColor(new Color(255, 230, 150));
        String sprintLabel = "CORRER";
        FontMetrics fmSprint = g2d.getFontMetrics();
        g2d.drawString(sprintLabel, sprintX - fmSprint.stringWidth(sprintLabel)/2, sprintY + sprintSize/2 + 20);
        
        // Indicador "Arraste para girar"
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2d.setFont(new Font("SansSerif", Font.ITALIC, 12));
        g2d.setColor(Color.WHITE);
        String dragHint = "↔ Arraste na tela para girar a câmera";
        FontMetrics fmDrag = g2d.getFontMetrics();
        g2d.drawString(dragHint, (W - fmDrag.stringWidth(dragHint))/2, H - 15);
        
        // Restaurar composite
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    private void drawTouchButton(Graphics2D g2d, Rectangle r, String symbol, boolean pressed, Color accentColor) {
        float alpha = pressed ? 0.55f : 0.25f;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        // Fundo do botão
        if (pressed) {
            g2d.setColor(accentColor.darker());
        } else {
            g2d.setColor(new Color(20, 20, 40));
        }
        g2d.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);
        
        // Borda
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pressed ? 0.8f : 0.4f));
        g2d.setColor(pressed ? accentColor : new Color(150, 180, 220));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(r.x, r.y, r.width, r.height, 14, 14);
        
        // Brilho superior
        if (pressed) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(r.x, r.y, r.width, r.height/2, 14, 14);
        }
        
        // Símbolo
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pressed ? 1.0f : 0.6f));
        g2d.setColor(pressed ? Color.WHITE : new Color(200, 220, 255));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(symbol, r.x + (r.width - fm.stringWidth(symbol))/2, r.y + (r.height + fm.getAscent() - fm.getDescent())/2);
    }
    
    private void drawTouchButtonCircle(Graphics2D g2d, int cx, int cy, int size, String symbol, boolean pressed, Color accentColor) {
        float alpha = pressed ? 0.55f : 0.25f;
        int r = size / 2;
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        // Fundo circular
        if (pressed) {
            g2d.setColor(accentColor.darker());
        } else {
            g2d.setColor(new Color(30, 25, 15));
        }
        g2d.fillOval(cx - r, cy - r, size, size);
        
        // Borda
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pressed ? 0.8f : 0.4f));
        g2d.setColor(pressed ? accentColor : new Color(200, 180, 100));
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawOval(cx - r, cy - r, size, size);
        
        // Glow quando pressionado
        if (pressed) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
            g2d.setColor(accentColor);
            g2d.fillOval(cx - r - 4, cy - r - 4, size + 8, size + 8);
        }
        
        // Símbolo
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pressed ? 1.0f : 0.6f));
        g2d.setColor(pressed ? Color.WHITE : new Color(255, 230, 150));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 28));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(symbol, cx - fm.stringWidth(symbol)/2, cy + (fm.getAscent() - fm.getDescent())/2);
    }
    
    private long lastHoleTeleportTime = 0;
    private float holeEffectAlpha = 0f;

    public void teleportPlayerToRandomLocation() {
        long now = System.currentTimeMillis();
        if (now - lastHoleTeleportTime < 1500) return;
        lastHoleTeleportTime = now;
        holeEffectAlpha = 1.0f;

        for (int attempt = 0; attempt < 200; attempt++) {
            int rx = (int)(Math.random() * Map.MAP_WIDTH);
            int ry = (int)(Math.random() * Map.MAP_HEIGHT);
            if (Map.isWalkable(rx, ry) && Map.map[ry][rx] != 7 && (rx != (int)camera.xPos || ry != (int)camera.yPos)) {
                camera.xPos = rx + 0.5;
                camera.yPos = ry + 0.5;
                System.out.println("Você caiu no buraco! Teleportado para: (" + rx + ", " + ry + ")");
                return;
            }
        }
    }

    private void renderDirectionIndicator(Graphics g, int cw) {
        if (health <= 0 || won || paused || inCutscene) return;

        double targetX = -1, targetY = -1;
        boolean isExitPortal = false;

        if (robux >= 50) {
            for (Entity e : entities) {
                if (e.active && e.type == 2) {
                    targetX = e.x;
                    targetY = e.y;
                    isExitPortal = true;
                    break;
                }
            }
        }

        if (!isExitPortal) {
            double minDist = Double.MAX_VALUE;
            for (Entity e : entities) {
                if (e.active && e.type == 1) {
                    double dx = e.x - camera.xPos;
                    double dy = e.y - camera.yPos;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < minDist) {
                        minDist = dist;
                        targetX = e.x;
                        targetY = e.y;
                    }
                }
            }
        }

        if (targetX == -1) return;

        double dx = targetX - camera.xPos;
        double dy = targetY - camera.yPos;
        double dist = Math.sqrt(dx * dx + dy * dy);
        int distInt = (int) Math.round(dist);

        double targetAngle = Math.atan2(dy, dx);
        double playerAngle = Math.atan2(camera.yDir, camera.xDir);
        double diff = targetAngle - playerAngle;
        while (diff < -Math.PI) diff += 2 * Math.PI;
        while (diff > Math.PI) diff -= 2 * Math.PI;
        double deg = Math.toDegrees(diff);

        String arrowSymbol;
        if (deg >= -22.5 && deg < 22.5) arrowSymbol = "⬆";
        else if (deg >= 22.5 && deg < 67.5) arrowSymbol = "↗";
        else if (deg >= 67.5 && deg < 112.5) arrowSymbol = "➡";
        else if (deg >= 112.5 && deg < 157.5) arrowSymbol = "↘";
        else if (deg >= -67.5 && deg < -22.5) arrowSymbol = "↖";
        else if (deg >= -112.5 && deg < -67.5) arrowSymbol = "⬅";
        else if (deg >= -157.5 && deg < -112.5) arrowSymbol = "↙";
        else arrowSymbol = "⬇";

        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 15f));
        FontMetrics fm = g2.getFontMetrics();

        String label;
        Color labelColor;
        Color bgBoxColor;

        if (isExitPortal) {
            label = "SAÍDA DA BACKROOM: " + arrowSymbol + " (" + distInt + "m)";
            labelColor = new Color(80, 255, 120);
            bgBoxColor = new Color(0, 50, 20, 180);
        } else {
            label = "ROBUX PRÓXIMO: " + arrowSymbol + " (" + distInt + "m)";
            labelColor = new Color(255, 220, 80);
            bgBoxColor = new Color(40, 35, 10, 170);
        }

        int boxW = fm.stringWidth(label) + 24;
        int boxH = 28;
        int boxX = cw - boxW / 2;
        int boxY = 15;

        g2.setColor(bgBoxColor);
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 14, 14);
        g2.setColor(labelColor);
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 14, 14);
        g2.drawString(label, boxX + 12, boxY + 19);
    }
    
    private void spawnPortal() {
        if (portalSpawned) return;
        portalSpawned = true;
        
        for (int attempt = 0; attempt < 200; attempt++) {
            int mx = (int)(Math.random() * Map.MAP_WIDTH);
            int my = (int)(Math.random() * Map.MAP_HEIGHT);
            double dx = mx - camera.xPos;
            double dy = my - camera.yPos;
            if (Map.isWalkable(mx, my) && Map.map[my][mx] != 7 && (dx*dx + dy*dy > 49)) {
                toAddEntities.add(new Entity(mx + 0.5, my + 0.5, portalTexture, 2));
                return;
            }
        }

        for (int my = 1; my < Map.MAP_HEIGHT - 1; my++) {
            for (int mx = 1; mx < Map.MAP_WIDTH - 1; mx++) {
                if (Map.isWalkable(mx, my) && Map.map[my][mx] != 7 && (mx != (int)camera.xPos || my != (int)camera.yPos)) {
                    toAddEntities.add(new Entity(mx + 0.5, my + 0.5, portalTexture, 2));
                    return;
                }
            }
        }
    }
    
    public void run() {
        long lastTime = System.nanoTime();
        final double ns = 1000000000.0 / 60.0;
        double delta = 0;
        requestFocus();
        
        while(running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            
            if (inCutscene) {
                if (cutsceneStartTime == 0) cutsceneStartTime = System.currentTimeMillis();
                long elapsed = System.currentTimeMillis() - cutsceneStartTime;
                
                if (camera.quit) { closeGame(); return; }
                if ((camera.esc || camera.space) && elapsed < 12000) {
                     cutsceneStartTime = System.currentTimeMillis() - 12000;
                     elapsed = 12000;
                }
                
                if (elapsed > 15000) {
                    inCutscene = false;
                }
                
                if (elapsed < 12000) {
                    while (delta >= 1) delta--; // Limpar os ticks de física
                    render();
                    continue;
                }
            }
            
            boolean escPressed = camera.esc;
            if (escPressed && !lastEsc && health > 0 && !won) {
                paused = !paused; 
            }
            lastEsc = escPressed;
            
            if (camera.quit) { closeGame(); return; }
            
            // Revive
            if (health <= 0 && camera.revive) {
                health = 100;
                stamina = 100;
                camera.xPos = startX;
                camera.yPos = startY;
                monster.x = 14.5;
                monster.y = 14.5;
            }
            
            while (delta >= 1) {
                if (health > 0 && !paused && !won) {
                    // Sincronizar controles touch
                    syncTouchToCamera();
                    
                    // Lógica de corrida (teclado OU touch)
                    if ((camera.run || touchRun) && stamina > 0) {
                        camera.MOVE_SPEED = 0.08;
                        stamina -= 0.5;
                    } else {
                        camera.MOVE_SPEED = 0.04;
                        if (stamina < 100) stamina += 0.2;
                    }
                    
                    camera.update();
                    
                    // Verificar se o jogador pisou em um buraco no chão (tile 7)
                    int px = (int) camera.xPos;
                    int py = (int) camera.yPos;
                    if (px >= 0 && px < Map.MAP_WIDTH && py >= 0 && py < Map.MAP_HEIGHT) {
                        if (Map.map[py][px] == 7) {
                            teleportPlayerToRandomLocation();
                        }
                    }
                    
                    for (Entity e : entities) {
                        e.update(camera);
                    }
                    
                    // Colisões e Interações
                    for (Entity e : entities) {
                        if (!e.active) continue;
                        
                        double dx = camera.xPos - e.x;
                        double dy = camera.yPos - e.y;
                        double dist = Math.sqrt(dx*dx + dy*dy);
                        
                        if (e.type == 0 && dist < 0.8) { // Monstro
                            health -= 0.5;
                        } else if (e.type == 1 && dist < 0.6) { // Moeda
                            e.active = false;
                            robux++;
                            
                            // Aumenta a velocidade do monstro a cada moeda
                            monster.speed = 0.02 + (robux * 0.0005);
                            
                            // A cada 5 moedas o monstro spawna (teleporta) para mais perto
                            if (robux % 5 == 0) {
                                teleportMonsterCloser();
                            }
                            
                            if (robux >= 50 && !portalSpawned) {
                                spawnPortal();
                            }
                        } else if (e.type == 2 && dist < 0.8) { // Portal
                            won = true;
                        }
                    }

                    // Adição segura de entidades recém-criadas fora do loop de iteração
                    if (!toAddEntities.isEmpty()) {
                        entities.addAll(toAddEntities);
                        toAddEntities.clear();
                    }
                }
                delta--;
            }
            
            if (!paused) {
                screen.update(camera, pixels);
                if (health > 0 && !won) {
                    // Ordenar entidades da mais longe para a mais perto para desenhar corretamente
                    List<Entity> toDraw = new ArrayList<>();
                    for (Entity e : entities) {
                        if (e.active) toDraw.add(e);
                    }
                    Collections.sort(toDraw, new Comparator<Entity>() {
                        public int compare(Entity e1, Entity e2) {
                            double dist1 = (camera.xPos - e1.x)*(camera.xPos - e1.x) + (camera.yPos - e1.y)*(camera.yPos - e1.y);
                            double dist2 = (camera.xPos - e2.x)*(camera.xPos - e2.x) + (camera.yPos - e2.y)*(camera.yPos - e2.y);
                            return Double.compare(dist2, dist1);
                        }
                    });
                    
                    for (Entity e : toDraw) {
                        screen.renderSprite(camera, e, pixels);
                    }
                }
            }
            render();
            
            // Mouse lock
            if (robot != null && hasFocus() && !paused && health > 0 && !won) {
                if (centerX == 0 && centerY == 0 && isDisplayable()) {
                    try {
                        centerX = getLocationOnScreen().x + getWidth() / 2;
                        centerY = getLocationOnScreen().y + getHeight() / 2;
                    } catch (Exception ex) {}
                }
                if (centerX != 0 && centerY != 0) {
                    robot.mouseMove(centerX, centerY);
                    try {
                        camera.lastMouseX = centerX - getLocationOnScreen().x;
                    } catch (Exception ex) {}
                }
            }
        }
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("The Backrooms - Java 3D");
        Game game = new Game();
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(game);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        game.requestFocus();
    }
}
