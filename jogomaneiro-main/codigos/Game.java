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
            parentFrame.encerrarMinigame();
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
    public int totalCoins = 50;

    // === CONTROLES TOUCH REDESENHADOS (SEM SOBREPOSIÇÃO) ===
    private boolean touchForward = false, touchBack = false, touchLeft = false, touchRight = false;
    private boolean touchRun = false;
    private int touchDragLastX = -1;
    private boolean isDraggingCamera = false;
    // Retângulos precisos e não-sobrepostos dos botões virtuais
    private Rectangle btnTouchUp, btnTouchDown, btnTouchLeft2, btnTouchRight2, btnTouchSprint;

    // === BOTÕES DE CLIQUE EM TELA PARA TODAS AS AÇÕES ===
    private Rectangle btnPularCutscene;
    private boolean btnPularCutsceneHover = false;
    private Rectangle btnPausarHUD;
    private boolean btnPausarHUDHover = false;
    private Rectangle btnPauseContinuar, btnPauseSair;
    private boolean btnPauseContinuarHover = false, btnPauseSairHover = false;
    private Rectangle btnMorteTentar, btnMorteSair;
    private boolean btnMorteTentarHover = false, btnMorteSairHover = false;

    // === TELA DE VITÓRIA E CONFETES ===
    public long victoryStartTime = 0;
    private Rectangle btnVoltarHobbyQuest;
    private boolean btnVoltarHover = false;
    private List<Confetti> confettiList = new ArrayList<>();
    private Cursor invisibleCursor;

    private static class Confetti {
        double x, y;
        double vx, vy;
        double rot, vRot;
        int sizeW, sizeH;
        Color color;
        int shape; // 0=retângulo, 1=círculo, 2=fitilho

        Confetti(int width, int height) {
            reset(width, height, true);
        }

        void reset(int width, int height, boolean initial) {
            x = Math.random() * width;
            y = initial ? Math.random() * height : -15 - Math.random() * 40;
            vx = (Math.random() - 0.5) * 3.5;
            vy = 2.2 + Math.random() * 4.0;
            rot = Math.random() * 360;
            vRot = (Math.random() - 0.5) * 10.0;
            sizeW = 8 + (int)(Math.random() * 8);
            sizeH = 5 + (int)(Math.random() * 6);
            shape = (int)(Math.random() * 3);
            Color[] palette = {
                new Color(255, 65, 120), new Color(255, 215, 0), new Color(46, 204, 113),
                new Color(52, 152, 219), new Color(155, 89, 182), new Color(241, 196, 15),
                new Color(230, 126, 34), new Color(26, 188, 156), new Color(255, 255, 255)
            };
            color = palette[(int)(Math.random() * palette.length)];
        }

        void update(int width, int height) {
            x += vx + Math.sin(y * 0.04) * 1.2;
            y += vy;
            rot += vRot;
            if (y > height + 20) {
                reset(width, height, false);
            }
        }

        void draw(Graphics2D g2) {
            Graphics2D g = (Graphics2D) g2.create();
            g.translate(x, y);
            g.rotate(Math.toRadians(rot));
            g.setColor(color);
            if (shape == 0) {
                g.fillRect(-sizeW/2, -sizeH/2, sizeW, sizeH);
            } else if (shape == 1) {
                g.fillOval(-sizeW/2, -sizeH/2, sizeW, sizeH);
            } else {
                g.fillRoundRect(-sizeW/2, -sizeH/2, sizeW + 4, Math.max(3, sizeH / 2), 3, 3);
            }
            g.dispose();
        }
    }

    private void initConfetti() {
        confettiList.clear();
        int w = Math.max(960, getWidth());
        int h = Math.max(720, getHeight());
        for (int i = 0; i < 110; i++) {
            confettiList.add(new Confetti(w, h));
        }
    }

    public Game() {
        thread = new Thread(this);
        image = new BufferedImage(960, 720, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
        camera = new Camera(startX, startY, 1, 0, 0, -0.60);
        
        try {
            String path = JogoAudrey.resolvePath("1000132930.jpg");
            if (path != null && new java.io.File(path).exists()) {
                cutsceneImg = javax.imageio.ImageIO.read(new java.io.File(path));
            }
        } catch (Exception e) {
            System.out.println("Aviso: Imagem 1000132930.jpg não encontrada.");
        }
        
        String wallPath = JogoAudrey.resolvePath("wall.png");
        texture = new Texture(wallPath != null ? wallPath : "wall.png", 128);

        String monsterPath = JogoAudrey.resolvePath("1000132829-removebg-preview.png");
        if (monsterPath == null || !new java.io.File(monsterPath).exists()) {
            monsterPath = JogoAudrey.resolvePath("monster.png");
        }
        monsterTexture = new Texture(monsterPath != null ? monsterPath : "monster.png", 512);

        String coinPath = JogoAudrey.resolvePath("moeda_cropped.png");
        if (coinPath == null || !new java.io.File(coinPath).exists()) {
            coinPath = JogoAudrey.resolvePath("coin.png");
        }
        coinTexture = new Texture(coinPath != null ? coinPath : "coin.png", 256);

        String portalPath = JogoAudrey.resolvePath("portal.png");
        portalTexture = new Texture(portalPath != null ? portalPath : "portal.png", 128);
        
        screen = new Screen(Map.map, 960, 720, texture);
        
        setupEntities();
        
        try {
            robot = new Robot();
        } catch (Exception e) { e.printStackTrace(); }
        
        BufferedImage blankCursor = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        invisibleCursor = Toolkit.getDefaultToolkit().createCustomCursor(blankCursor, new Point(0, 0), "invisible");
        setCursor(invisibleCursor);
        
        addKeyListener(camera);
        addMouseMotionListener(camera);
        
        setBackground(Color.black);
        
        centerX = 0;
        centerY = 0;
        
        // Controles touch & clique nos botões em tela
        addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mx = e.getX(), my = e.getY();
                
                // 1. Pular Cutscene clicando no botão na tela
                if (inCutscene) {
                    if (btnPularCutscene != null && btnPularCutscene.contains(mx, my)) {
                        cutsceneStartTime = System.currentTimeMillis() - 15000;
                        inCutscene = false;
                        return;
                    }
                }
                
                // 2. Vitória: Voltar pro Hobby Quest clicando no botão
                if (won) {
                    if (btnVoltarHobbyQuest != null && btnVoltarHobbyQuest.contains(mx, my)) {
                        closeGame();
                        return;
                    }
                }
                
                // 3. Morte: Tentar de novo ou Sair clicando nos botões
                if (health <= 0) {
                    if (btnMorteTentar != null && btnMorteTentar.contains(mx, my)) {
                        health = 100;
                        stamina = 100;
                        camera.xPos = startX;
                        camera.yPos = startY;
                        monster.x = 14.5;
                        monster.y = 14.5;
                        return;
                    }
                    if (btnMorteSair != null && btnMorteSair.contains(mx, my)) {
                        closeGame();
                        return;
                    }
                }
                
                // 4. Menu de Pausa: Continuar ou Sair clicando nos botões
                if (paused) {
                    if (btnPauseContinuar != null && btnPauseContinuar.contains(mx, my)) {
                        paused = false;
                        return;
                    }
                    if (btnPauseSair != null && btnPauseSair.contains(mx, my)) {
                        closeGame();
                        return;
                    }
                }
                
                // 5. Botão de Pausa no HUD durante o jogo
                if (health > 0 && !paused && !won && !inCutscene) {
                    if (btnPausarHUD != null && btnPausarHUD.contains(mx, my)) {
                        paused = true;
                        return;
                    }
                }

                handleTouchPress(mx, my, true);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
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
        
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleTouchDrag(e.getX(), e.getY());
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                int mx = e.getX(), my = e.getY();
                boolean overButton = false;
                
                if (inCutscene) {
                    btnPularCutsceneHover = (btnPularCutscene != null && btnPularCutscene.contains(mx, my));
                    overButton = btnPularCutsceneHover;
                } else if (won) {
                    btnVoltarHover = (btnVoltarHobbyQuest != null && btnVoltarHobbyQuest.contains(mx, my));
                    overButton = btnVoltarHover;
                } else if (health <= 0) {
                    btnMorteTentarHover = (btnMorteTentar != null && btnMorteTentar.contains(mx, my));
                    btnMorteSairHover = (btnMorteSair != null && btnMorteSair.contains(mx, my));
                    overButton = btnMorteTentarHover || btnMorteSairHover;
                } else if (paused) {
                    btnPauseContinuarHover = (btnPauseContinuar != null && btnPauseContinuar.contains(mx, my));
                    btnPauseSairHover = (btnPauseSair != null && btnPauseSair.contains(mx, my));
                    overButton = btnPauseContinuarHover || btnPauseSairHover;
                } else {
                    btnPausarHUDHover = (btnPausarHUD != null && btnPausarHUD.contains(mx, my));
                    overButton = btnPausarHUDHover;
                }
                
                if (overButton || inCutscene || won || paused || health <= 0) {
                    setCursor(Cursor.getDefaultCursor());
                } else {
                    setCursor(invisibleCursor);
                }
            }
        });
        
        start();
    }
    
    private void handleTouchPress(int mx, int my, boolean pressed) {
        if (paused || health <= 0 || won || inCutscene) return;
        
        // Verificar botões de andar com limites estritos (não sobrepostos)
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
            // Arraste na tela para girar câmera
            isDraggingCamera = pressed;
            touchDragLastX = mx;
        }
        syncTouchToCamera();
    }
    
    private void handleTouchDrag(int mx, int my) {
        if (paused || health <= 0 || won || inCutscene) return;
        
        // Se arrastou para dentro de um botão do D-Pad
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
        totalCoins = 0;
        
        // Monstro
        monster = new Entity(14.5, 14.5, monsterTexture, 0);
        entities.add(monster);
        
        // ~90 Moedas espalhadas por todos os corredores e salas - meta: coletar 67 para vencer
        int count = 0;
        int maxAttempts = 10000;
        int attempts = 0;
        while (count < 90 && attempts < maxAttempts) {
            attempts++;
            int mx = 1 + (int)(Math.random() * (Map.MAP_WIDTH - 2));
            int my = 1 + (int)(Math.random() * (Map.MAP_HEIGHT - 2));
            if (Map.isWalkable(mx, my) && Map.map[my][mx] != 7 && (mx != (int)startX || my != (int)startY)) {
                boolean tooClose = false;
                for (Entity e : entities) {
                    if (e.type == 1) {
                        double dx = e.x - (mx + 0.5);
                        double dy = e.y - (my + 0.5);
                        if (Math.sqrt(dx*dx + dy*dy) < 1.5) { tooClose = true; break; }
                    }
                }
                if (!tooClose) {
                    entities.add(new Entity(mx + 0.5, my + 0.5, coinTexture, 1));
                    count++;
                }
            }
        }
        // Meta de vitória: 67 moedas (mesmo que haja mais espalhadas)
        totalCoins = 67;
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
            
            // Botão "PULAR CUTSCENE" clicável na tela
            int skipW = 160, skipH = 42;
            int skipX = getWidth() - skipW - 20;
            int skipY = 20;
            btnPularCutscene = new Rectangle(skipX, skipY, skipW, skipH);
            
            g2.setColor(btnPularCutsceneHover ? new Color(75, 65, 135, 240) : new Color(35, 25, 65, 200));
            g2.fillRoundRect(skipX, skipY, skipW, skipH, 20, 20);
            g2.setColor(btnPularCutsceneHover ? new Color(200, 180, 255) : new Color(130, 110, 200));
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(skipX, skipY, skipW, skipH, 20, 20);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            String skipTxt = "PULAR ⏩";
            FontMetrics fmSkip = g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            g2.drawString(skipTxt, skipX + (skipW - fmSkip.stringWidth(skipTxt))/2, skipY + 26);
            
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
        g2d.setColor(new Color(0, 0, 0, 140));
        g2d.fillRoundRect(barX - 2, barY - 2, barW + 4, barH + 4, 8, 8);
        g2d.setColor(new Color(80, 0, 0, 200));
        g2d.fillRoundRect(barX, barY, barW, barH, 6, 6);
        int hpW = (int)(Math.max(0, health) / 100.0 * barW);
        if (hpW > 0) {
            float hpFrac = (float)(health / 100.0);
            int hpR = (int)(255 * (1 - hpFrac));
            int hpG = (int)(200 * hpFrac);
            g2d.setColor(new Color(Math.min(255, hpR + 40), Math.min(200, hpG), 30));
            g2d.fillRoundRect(barX, barY, hpW, barH, 6, 6);
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

        // === BOTÃO PAUSAR NO HUD (CLICÁVEL) ===
        int pauseBtnW = 105, pauseBtnH = 30;
        int pauseBtnX = barX;
        int pauseBtnY = stY + 10 + 10;
        btnPausarHUD = new Rectangle(pauseBtnX, pauseBtnY, pauseBtnW, pauseBtnH);
        
        g2d.setColor(btnPausarHUDHover ? new Color(50, 75, 115, 220) : new Color(18, 28, 45, 180));
        g2d.fillRoundRect(pauseBtnX, pauseBtnY, pauseBtnW, pauseBtnH, 10, 10);
        g2d.setColor(btnPausarHUDHover ? new Color(140, 200, 255) : new Color(70, 130, 210));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(pauseBtnX, pauseBtnY, pauseBtnW, pauseBtnH, 10, 10);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2d.setColor(Color.WHITE);
        g2d.drawString("⏸ PAUSAR", pauseBtnX + 15, pauseBtnY + 20);

        // === CONTADOR DE ROBUX (canto superior direito) ===
        String robuxTxt = "◈  " + robux + " / " + totalCoins + " Robux";
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics fmR = g2d.getFontMetrics();
        int rxW = fmR.stringWidth(robuxTxt) + 24;
        int rxX = getWidth() - rxW - 14;
        int rxY = 12;
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRoundRect(rxX - 4, rxY, rxW, 28, 14, 14);
        g2d.setColor(new Color(255, 210, 0, 60));
        g2d.setStroke(new java.awt.BasicStroke(1.5f));
        g2d.drawRoundRect(rxX - 4, rxY, rxW, 28, 14, 14);
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.drawString(robuxTxt, rxX + 9, rxY + 20);
        g2d.setColor(new Color(255, 210, 0));
        g2d.drawString(robuxTxt, rxX + 8, rxY + 19);

        // === INDICADOR DE DIREÇÃO ===
        renderDirectionIndicator(g, cw);

        // === MIRA CENTRAL ===
        g2d.setStroke(new java.awt.BasicStroke(1.5f));
        g2d.setColor(new Color(255, 255, 255, 40));
        g2d.drawLine(cw - 8, ch, cw + 8, ch);
        g2d.drawLine(cw, ch - 8, cw, ch + 8);
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.drawLine(cw - 5, ch, cw + 5, ch);
        g2d.drawLine(cw, ch - 5, cw, ch + 5);
        g2d.setColor(new Color(255, 255, 255, 255));
        g2d.fillOval(cw - 1, ch - 1, 3, 3);
        g2d.setStroke(new java.awt.BasicStroke(1f));

        // === 3ª CUTSCENE / TELA DE VITÓRIA COM CONFETES E +10000 DE AURA ===
        if (won) {
            renderVictoryCutscene(g2d, cw, ch);

        // === TELA DE MORTE COM BOTÕES CLICÁVEIS ===
        } else if (health <= 0) {
            g2d.setColor(new Color(100, 0, 0, 210));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            int dCardW = Math.min(520, getWidth() - 40);
            int dCardH = 260;
            int dCardX = cw - dCardW / 2;
            int dCardY = ch - dCardH / 2;
            
            // Card escuro
            g2d.setColor(new Color(25, 8, 8, 245));
            g2d.fillRoundRect(dCardX, dCardY, dCardW, dCardH, 24, 24);
            g2d.setColor(new Color(255, 60, 60, 200));
            g2d.setStroke(new BasicStroke(2.5f));
            g2d.drawRoundRect(dCardX, dCardY, dCardW, dCardH, 24, 24);
            
            // Título
            g2d.setFont(new Font("SansSerif", Font.BOLD, 36));
            String dTxt = "VOCÊ FOI MOGGADO";
            g2d.setColor(new Color(255, 80, 80));
            g2d.drawString(dTxt, cw - g2d.getFontMetrics().stringWidth(dTxt)/2, dCardY + 60);
            
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
            String dSub = "O Marlon te alcançou nas Backrooms...";
            g2d.setColor(new Color(240, 180, 180));
            g2d.drawString(dSub, cw - g2d.getFontMetrics().stringWidth(dSub)/2, dCardY + 98);
            
            // Botão 1: TENTAR NOVAMENTE
            int mbW = 210, mbH = 48;
            int mb1X = cw - mbW - 10;
            int mb2X = cw + 10;
            int mbY = dCardY + 155;
            
            btnMorteTentar = new Rectangle(mb1X, mbY, mbW, mbH);
            g2d.setColor(btnMorteTentarHover ? new Color(45, 150, 75) : new Color(25, 100, 50));
            g2d.fillRoundRect(mb1X, mbY, mbW, mbH, 14, 14);
            g2d.setColor(btnMorteTentarHover ? new Color(160, 255, 190) : new Color(70, 200, 120));
            g2d.setStroke(new BasicStroke(btnMorteTentarHover ? 2.5f : 1.5f));
            g2d.drawRoundRect(mb1X, mbY, mbW, mbH, 14, 14);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2d.setColor(Color.WHITE);
            String mtTxt = "🔄 TENTAR DE NOVO [R]";
            g2d.drawString(mtTxt, mb1X + (mbW - g2d.getFontMetrics().stringWidth(mtTxt))/2, mbY + 29);
            
            // Botão 2: VOLTAR AO HOBBY QUEST
            btnMorteSair = new Rectangle(mb2X, mbY, mbW, mbH);
            g2d.setColor(btnMorteSairHover ? new Color(175, 45, 45) : new Color(120, 25, 25));
            g2d.fillRoundRect(mb2X, mbY, mbW, mbH, 14, 14);
            g2d.setColor(btnMorteSairHover ? new Color(255, 170, 170) : new Color(220, 90, 90));
            g2d.setStroke(new BasicStroke(btnMorteSairHover ? 2.5f : 1.5f));
            g2d.drawRoundRect(mb2X, mbY, mbW, mbH, 14, 14);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2d.setColor(Color.WHITE);
            String msTxt = "➔ HOBBY QUEST [Q]";
            g2d.drawString(msTxt, mb2X + (mbW - g2d.getFontMetrics().stringWidth(msTxt))/2, mbY + 29);
        }

        // === MENU DE PAUSA COM BOTÕES CLICÁVEIS ===
        if (paused && health > 0 && !won) {
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            int pw2 = 380, ph2 = 230;
            int px = cw - pw2/2, py = ch - ph2/2;
            
            // Card
            g2d.setColor(new Color(15, 22, 35, 245));
            g2d.fillRoundRect(px, py, pw2, ph2, 24, 24);
            g2d.setColor(new Color(90, 160, 255, 180));
            g2d.setStroke(new java.awt.BasicStroke(2f));
            g2d.drawRoundRect(px, py, pw2, ph2, 24, 24);
            
            // Título
            g2d.setFont(new Font("SansSerif", Font.BOLD, 28));
            String pTxt = "JOGO PAUSADO";
            int ptw = g2d.getFontMetrics().stringWidth(pTxt);
            g2d.setColor(Color.WHITE);
            g2d.drawString(pTxt, cw - ptw/2, py + 48);
            
            // Botão 1: CONTINUAR
            int bW = 300, bH = 46;
            int bX = cw - bW/2;
            int b1Y = py + 80;
            btnPauseContinuar = new Rectangle(bX, b1Y, bW, bH);
            g2d.setColor(btnPauseContinuarHover ? new Color(40, 160, 85) : new Color(25, 120, 65));
            g2d.fillRoundRect(bX, b1Y, bW, bH, 14, 14);
            g2d.setColor(btnPauseContinuarHover ? new Color(180, 255, 210) : new Color(80, 220, 140));
            g2d.setStroke(new BasicStroke(btnPauseContinuarHover ? 2.5f : 1.5f));
            g2d.drawRoundRect(bX, b1Y, bW, bH, 14, 14);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2d.setColor(Color.WHITE);
            String b1Txt = "▶  CONTINUAR  [ ESC ]";
            g2d.drawString(b1Txt, cw - g2d.getFontMetrics().stringWidth(b1Txt)/2, b1Y + 28);
            
            // Botão 2: SAIR
            int b2Y = py + 145;
            btnPauseSair = new Rectangle(bX, b2Y, bW, bH);
            g2d.setColor(btnPauseSairHover ? new Color(170, 45, 45) : new Color(120, 30, 30));
            g2d.fillRoundRect(bX, b2Y, bW, bH, 14, 14);
            g2d.setColor(btnPauseSairHover ? new Color(255, 160, 160) : new Color(220, 90, 90));
            g2d.setStroke(new BasicStroke(btnPauseSairHover ? 2.5f : 1.5f));
            g2d.drawRoundRect(bX, b2Y, bW, bH, 14, 14);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2d.setColor(Color.WHITE);
            String b2Txt = "➔  SAIR PRO HOBBY QUEST  [ Q ]";
            g2d.drawString(b2Txt, cw - g2d.getFontMetrics().stringWidth(b2Txt)/2, b2Y + 28);
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

        // === CONTROLES TOUCH VIRTUAIS (ESPACADOS E MODERNOS) ===
        if (health > 0 && !won && !paused && !inCutscene) {
            renderTouchControls(g2d);
        }

        bs.show();
    }
    
    // === RENDERIZAÇÃO DA 3ª CUTSCENE: VITÓRIA + CONFETES + 10.000 AURA ===
    private void renderVictoryCutscene(Graphics2D g2d, int cw, int ch) {
        int W = getWidth(), H = getHeight();
        
        // Fundo escurecido com gradiente suave
        g2d.setColor(new Color(5, 15, 10, 220));
        g2d.fillRect(0, 0, W, H);
        
        // Confetes animados caindo pela tela
        if (confettiList.isEmpty()) initConfetti();
        for (Confetti c : confettiList) {
            c.update(W, H);
            c.draw(g2d);
        }
        
        // Card Central de Vitória
        int cardW = Math.min(680, W - 40);
        int cardH = Math.min(420, H - 60);
        int cardX = (W - cardW) / 2;
        int cardY = (H - cardH) / 2;
        
        // Sombra do card
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRoundRect(cardX + 8, cardY + 8, cardW, cardH, 28, 28);
        
        // Fundo do card com efeito de vidro escuro
        GradientPaint cardGrad = new GradientPaint(
            cardX, cardY, new Color(18, 28, 24, 245),
            cardX, cardY + cardH, new Color(10, 18, 14, 250)
        );
        g2d.setPaint(cardGrad);
        g2d.fillRoundRect(cardX, cardY, cardW, cardH, 28, 28);
        
        // Borda dourada brilhante com glow
        g2d.setColor(new Color(255, 215, 0, 180));
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawRoundRect(cardX, cardY, cardW, cardH, 28, 28);
        
        // 1. Tag de Conquista / Ícone
        g2d.setFont(new Font("SansSerif", Font.BOLD, 15));
        String badge = "★ CONQUISTA DESBLOQUEADA ★";
        FontMetrics fmBadge = g2d.getFontMetrics();
        int bw = fmBadge.stringWidth(badge) + 28;
        int bx = cw - bw / 2;
        int by = cardY + 32;
        g2d.setColor(new Color(255, 215, 0, 40));
        g2d.fillRoundRect(bx, by - 16, bw, 26, 13, 13);
        g2d.setColor(new Color(255, 215, 0, 180));
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawRoundRect(bx, by - 16, bw, 26, 13, 13);
        g2d.setColor(new Color(255, 225, 90));
        g2d.drawString(badge, bx + 14, by + 2);
        
        // 2. Título Principal
        g2d.setFont(new Font("SansSerif", Font.BOLD, Math.max(24, (int)(cardW * 0.046))));
        FontMetrics fmTitle = g2d.getFontMetrics();
        String titleTxt = "Você conseguiu escapar do Marlon!";
        int tw = fmTitle.stringWidth(titleTxt);
        
        // Sombra do título
        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.drawString(titleTxt, cw - tw/2 + 2, cardY + 95 + 2);
        // Texto principal em verde menta / branco brilhante
        g2d.setColor(new Color(130, 255, 180));
        g2d.drawString(titleTxt, cw - tw/2, cardY + 95);
        
        // 3. Pílula de Recompensa de Aura (+10000 DE AURA)
        int auraW = Math.min(340, cardW - 60);
        int auraH = 54;
        int auraX = cw - auraW / 2;
        int auraY = cardY + 130;
        
        // Fundo do banner de aura
        GradientPaint auraGrad = new GradientPaint(
            auraX, auraY, new Color(255, 195, 0, 230),
            auraX + auraW, auraY, new Color(255, 120, 0, 230)
        );
        g2d.setPaint(auraGrad);
        g2d.fillRoundRect(auraX, auraY, auraW, auraH, 20, 20);
        
        // Glow / Borda da aura
        g2d.setColor(new Color(255, 255, 255, 190));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(auraX, auraY, auraW, auraH, 20, 20);
        
        // Texto da aura
        g2d.setFont(new Font("SansSerif", Font.BOLD, 26));
        FontMetrics fmAura = g2d.getFontMetrics();
        String auraTxt = "+10.000 DE AURA ⚡";
        int aw = fmAura.stringWidth(auraTxt);
        g2d.setColor(new Color(40, 20, 0, 180));
        g2d.drawString(auraTxt, cw - aw/2 + 2, auraY + 36 + 2);
        g2d.setColor(Color.WHITE);
        g2d.drawString(auraTxt, cw - aw/2, auraY + 36);
        
        // 4. Detalhes / Subtítulo
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 15));
        FontMetrics fmSub = g2d.getFontMetrics();
        String subTxt = "Você coletou " + totalCoins + " moedas e escapou das Backrooms!";
        int sw = fmSub.stringWidth(subTxt);
        g2d.setColor(new Color(200, 240, 220, 220));
        g2d.drawString(subTxt, cw - sw/2, cardY + 225);
        
        // 5. Botão "VOLTAR PRO HOBBY QUEST"
        int btnW = Math.min(360, cardW - 80);
        int btnH = 50;
        int btnX = cw - btnW / 2;
        int btnY = cardY + cardH - 85;
        btnVoltarHobbyQuest = new Rectangle(btnX, btnY, btnW, btnH);
        
        // Fundo do Botão (com efeito hover)
        if (btnVoltarHover) {
            g2d.setColor(new Color(40, 160, 90));
        } else {
            g2d.setColor(new Color(25, 120, 65));
        }
        g2d.fillRoundRect(btnX, btnY, btnW, btnH, 16, 16);
        
        // Borda do Botão
        g2d.setColor(btnVoltarHover ? new Color(160, 255, 200) : new Color(80, 220, 140));
        g2d.setStroke(new BasicStroke(btnVoltarHover ? 2.5f : 1.5f));
        g2d.drawRoundRect(btnX, btnY, btnW, btnH, 16, 16);
        
        // Texto do Botão
        g2d.setFont(new Font("SansSerif", Font.BOLD, 17));
        FontMetrics fmBtn = g2d.getFontMetrics();
        String btnTxt = "VOLTAR PRO HOBBY QUEST  ➔";
        int btw = fmBtn.stringWidth(btnTxt);
        g2d.setColor(Color.WHITE);
        g2d.drawString(btnTxt, cw - btw/2, btnY + 31);
        
        // Atalho
        g2d.setFont(new Font("SansSerif", Font.ITALIC, 12));
        g2d.setColor(new Color(170, 200, 185, 160));
        String hintTxt = "Pressione [ ENTER ], [ ESPAÇO ] ou clique no botão acima";
        int hw = g2d.getFontMetrics().stringWidth(hintTxt);
        g2d.drawString(hintTxt, cw - hw/2, cardY + cardH - 16);
    }
    
    // === CONTROLES TOUCH REDESENHADOS E AMPLAMENTE ESPAÇADOS ===
    private void renderTouchControls(Graphics2D g2d) {
        int W = getWidth(), H = getHeight();
        
        // === D-PAD EM CRUZ AMPLAMENTE ESPAÇADO (SEM NENHUMA SOBREPOSIÇÃO) ===
        int dpadCenterX = 155;
        int dpadCenterY = H - 155;
        int btnSize = 60;
        int distFromCenter = 68;
        
        // Retângulos completamente isolados geometricamente
        btnTouchUp = new Rectangle(dpadCenterX - btnSize/2, dpadCenterY - distFromCenter - btnSize/2, btnSize, btnSize);
        btnTouchDown = new Rectangle(dpadCenterX - btnSize/2, dpadCenterY + distFromCenter - btnSize/2, btnSize, btnSize);
        btnTouchLeft2 = new Rectangle(dpadCenterX - distFromCenter - btnSize/2, dpadCenterY - btnSize/2, btnSize, btnSize);
        btnTouchRight2 = new Rectangle(dpadCenterX + distFromCenter - btnSize/2, dpadCenterY - btnSize/2, btnSize, btnSize);
        
        // Base de fundo do D-pad (disco e cruzeta suave)
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
        g2d.setColor(new Color(20, 30, 60));
        g2d.fillOval(dpadCenterX - 110, dpadCenterY - 110, 220, 220);
        g2d.setColor(new Color(80, 150, 240));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(dpadCenterX - 110, dpadCenterY - 110, 220, 220);
        
        // Linhas guias da cruzeta
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.20f));
        g2d.setColor(new Color(100, 180, 255));
        g2d.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(dpadCenterX, dpadCenterY - distFromCenter, dpadCenterX, dpadCenterY + distFromCenter);
        g2d.drawLine(dpadCenterX - distFromCenter, dpadCenterY, dpadCenterX + distFromCenter, dpadCenterY);
        
        // Núcleo central
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        g2d.setColor(new Color(40, 80, 150));
        g2d.fillOval(dpadCenterX - 18, dpadCenterY - 18, 36, 36);
        g2d.setColor(new Color(120, 200, 255));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(dpadCenterX - 18, dpadCenterY - 18, 36, 36);
        
        // Desenhar cada botão direcional do D-pad
        drawTouchButton(g2d, btnTouchUp, "▲", "W", touchForward, new Color(60, 165, 255));
        drawTouchButton(g2d, btnTouchDown, "▼", "S", touchBack, new Color(60, 165, 255));
        drawTouchButton(g2d, btnTouchLeft2, "◄", "A", touchLeft, new Color(60, 165, 255));
        drawTouchButton(g2d, btnTouchRight2, "►", "D", touchRight, new Color(60, 165, 255));
        
        // Label "ANDAR"
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.70f));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2d.setColor(new Color(210, 235, 255));
        String moveLabel = "MOVER (W/A/S/D)";
        FontMetrics fmMove = g2d.getFontMetrics();
        g2d.drawString(moveLabel, dpadCenterX - fmMove.stringWidth(moveLabel)/2, dpadCenterY + distFromCenter + btnSize/2 + 24);
        
        // === BOTÃO SPRINT (canto inferior direito) ===
        int sprintSize = 74;
        int sprintX = W - 140;
        int sprintY = H - 140;
        btnTouchSprint = new Rectangle(sprintX - sprintSize/2, sprintY - sprintSize/2, sprintSize, sprintSize);
        
        drawTouchButtonCircle(g2d, sprintX, sprintY, sprintSize, "⚡", "CORRER", touchRun, new Color(255, 195, 30));
        
        // Indicador "Arraste para girar"
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.40f));
        g2d.setFont(new Font("SansSerif", Font.ITALIC, 13));
        g2d.setColor(Color.WHITE);
        String dragHint = "↔ Toque e arraste em qualquer lugar para girar a câmera";
        FontMetrics fmDrag = g2d.getFontMetrics();
        g2d.drawString(dragHint, (W - fmDrag.stringWidth(dragHint))/2, H - 16);
        
        // Restaurar composite
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    
    private void drawTouchButton(Graphics2D g2d, Rectangle r, String symbol, String subKey, boolean pressed, Color accentColor) {
        float alpha = pressed ? 0.85f : 0.45f;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        // Sombra do botão
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRoundRect(r.x + 2, r.y + 2, r.width, r.height, 18, 18);
        
        // Fundo do botão
        if (pressed) {
            g2d.setColor(accentColor.darker());
        } else {
            g2d.setColor(new Color(15, 28, 55));
        }
        g2d.fillRoundRect(r.x, r.y, r.width, r.height, 18, 18);
        
        // Borda com glow
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pressed ? 1.0f : 0.70f));
        g2d.setColor(pressed ? Color.WHITE : accentColor);
        g2d.setStroke(new BasicStroke(pressed ? 3.0f : 2.0f));
        g2d.drawRoundRect(r.x, r.y, r.width, r.height, 18, 18);
        
        // Símbolo da Seta
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pressed ? 1.0f : 0.90f));
        g2d.setColor(pressed ? Color.WHITE : new Color(230, 245, 255));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(symbol, r.x + (r.width - fm.stringWidth(symbol))/2, r.y + (r.height / 2) + 2);
        
        // Tecla de atalho (W/A/S/D)
        g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics fmSub = g2d.getFontMetrics();
        g2d.setColor(pressed ? new Color(255, 255, 255, 200) : new Color(160, 210, 255, 170));
        g2d.drawString(subKey, r.x + (r.width - fmSub.stringWidth(subKey))/2, r.y + r.height - 7);
    }
    
    private void drawTouchButtonCircle(Graphics2D g2d, int cx, int cy, int size, String symbol, String label, boolean pressed, Color accentColor) {
        float alpha = pressed ? 0.85f : 0.45f;
        int r = size / 2;
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        // Sombra
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillOval(cx - r + 3, cy - r + 3, size, size);
        
        // Fundo
        if (pressed) {
            g2d.setColor(accentColor.darker());
        } else {
            g2d.setColor(new Color(45, 32, 10));
        }
        g2d.fillOval(cx - r, cy - r, size, size);
        
        // Borda
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pressed ? 1.0f : 0.75f));
        g2d.setColor(pressed ? Color.WHITE : accentColor);
        g2d.setStroke(new BasicStroke(pressed ? 3.5f : 2.5f));
        g2d.drawOval(cx - r, cy - r, size, size);
        
        // Símbolo do Raio
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pressed ? 1.0f : 0.95f));
        g2d.setColor(pressed ? Color.WHITE : new Color(255, 240, 140));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 28));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(symbol, cx - fm.stringWidth(symbol)/2, cy + 2);
        
        // Label CORRER
        g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics fmSub = g2d.getFontMetrics();
        g2d.setColor(pressed ? Color.WHITE : new Color(255, 230, 160));
        g2d.drawString(label, cx - fmSub.stringWidth(label)/2, cy + r - 8);
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

        if (robux >= totalCoins) {
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
                    while (delta >= 1) delta--;
                    render();
                    continue;
                }
            }
            
            boolean escPressed = camera.esc;
            if (escPressed && !lastEsc && health > 0 && !won) {
                paused = !paused; 
            }
            lastEsc = escPressed;
            
            // Vitória: teclas para voltar
            if (won) {
                if (camera.quit || camera.space || camera.esc) {
                    closeGame();
                    return;
                }
            } else {
                if (camera.quit) { closeGame(); return; }
            }
            
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
                        
                        if (e.type == 0 && dist < 0.8) { // Monstro Marlon
                            health -= 0.5;
                        } else if (e.type == 1 && dist < 0.6) { // Moeda
                            e.active = false;
                            robux++;
                            
                            // Aumenta a velocidade do monstro gradualmente a cada moeda
                            monster.speed = 0.02 + (robux * 0.0004);
                            
                            // A cada 10 moedas o monstro spawna (teleporta) para mais perto
                            if (robux % 10 == 0) {
                                teleportMonsterCloser();
                            }
                            
                            // Ao coletar 67 moedas: ativa a 3ª Cutscene de Vitória com confetes e +10000 de aura
                            if (robux >= totalCoins) {
                                won = true;
                                if (victoryStartTime == 0) {
                                    victoryStartTime = System.currentTimeMillis();
                                    initConfetti();
                                    setCursor(Cursor.getDefaultCursor());
                                }
                            }
                        } else if (e.type == 2 && dist < 0.8) { // Portal
                            won = true;
                            if (victoryStartTime == 0) {
                                victoryStartTime = System.currentTimeMillis();
                                initConfetti();
                                setCursor(Cursor.getDefaultCursor());
                            }
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
            
            // Mouse lock (apenas durante o gameplay)
            if (robot != null && hasFocus() && !paused && health > 0 && !won && !inCutscene) {
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
