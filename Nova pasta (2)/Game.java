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

    public Game() {
        thread = new Thread(this);
        image = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
        camera = new Camera(startX, startY, 1, 0, 0, -0.66);
        
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
        
        screen = new Screen(Map.map, 640, 480, texture);
        
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
        
        start();
    }
    
    private void setupEntities() {
        entities.clear();
        toAddEntities.clear();
        portalSpawned = false;
        
        // Monstro
        monster = new Entity(14.5, 14.5, monsterTexture, 0);
        entities.add(monster);
        
        // 120 Moedas (Robux) espalhadas pelo mapa
        int count = 0;
        while (count < 120) {
            int mx = (int)(Math.random() * Map.MAP_WIDTH);
            int my = (int)(Math.random() * Map.MAP_HEIGHT);
            if (Map.isWalkable(mx, my) && Map.map[my][mx] != 7 && (mx != (int)startX || my != (int)startY)) {
                boolean exists = false;
                for (Entity e : entities) {
                    if ((int)e.x == mx && (int)e.y == my) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
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
        
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        
        // Barra de Vida
        g.setColor(Color.RED);
        g.fillRect(20, 20, 200, 15);
        g.setColor(Color.GREEN);
        g.fillRect(20, 20, (int)(Math.max(0, health) * 2), 15);
        g.setColor(Color.WHITE);
        g.drawRect(20, 20, 200, 15);
        g.drawString("VIDA", 228, 32);
        
        // Barra de Stamina
        g.setColor(new Color(50, 50, 50));
        g.fillRect(20, 40, 200, 10);
        g.setColor(Color.CYAN);
        g.fillRect(20, 40, (int)(Math.max(0, stamina) * 2), 10);
        g.setColor(Color.WHITE);
        g.drawRect(20, 40, 200, 10);
        
        // Robux Counter
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(20f));
        g.drawString("Robux: " + robux + " / 50", getWidth() - 160, 35);
        if (robux >= 50 && !won) {
            g.setColor(Color.GREEN);
            g.drawString("O PORTAL APARECEU!", getWidth() - 220, 60);
        }
        
        int cw = getWidth() / 2;
        int ch = getHeight() / 2;
        
        // Indicador / Radar com seta de navegação (Robux / Portal de Saída)
        renderDirectionIndicator(g, cw);
        
        // Mira central
        g.setColor(Color.WHITE);
        g.drawLine(cw - 4, ch, cw + 4, ch);
        g.drawLine(cw, ch - 4, cw, ch + 4);
        
        if (won) {
            g.setColor(new Color(0, 150, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(32f));
            g.drawString("VOCE ESCAPOU!", cw - 120, ch - 20);
            g.setFont(g.getFont().deriveFont(16f));
            g.drawString("Pressione Q para sair", cw - 80, ch + 20);
        } else if (health <= 0) {
            // Fundo vermelho escuro com fade
            g.setColor(new Color(120, 0, 0, 200));
            g.fillRect(0, 0, getWidth(), getHeight());
            // Texto principal grande e centralizado
            g.setFont(g.getFont().deriveFont(Font.BOLD, 52f));
            String morte = "VOC\u00CA FOI MOGGADO";
            g.setColor(new Color(50, 0, 0));
            g.drawString(morte, cw - g.getFontMetrics().stringWidth(morte) / 2 + 3, ch - 20 + 3);
            g.setColor(Color.WHITE);
            g.drawString(morte, cw - g.getFontMetrics().stringWidth(morte) / 2, ch - 20);
            // Subítulo
            g.setFont(g.getFont().deriveFont(Font.PLAIN, 18f));
            String sub = "o monstro te pegou...";
            g.setColor(new Color(200, 150, 150));
            g.drawString(sub, cw - g.getFontMetrics().stringWidth(sub) / 2, ch + 20);
            // Instrucao
            g.setFont(g.getFont().deriveFont(Font.ITALIC, 14f));
            String inst = "Pressione R para tentar de novo";
            g.setColor(new Color(180, 180, 180));
            g.drawString(inst, cw - g.getFontMetrics().stringWidth(inst) / 2, ch + 55);
        }
        
        // Menu de Pausa
        if (paused && health > 0 && !won) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(40f));
            g.drawString("JOGO PAUSADO", cw - 160, ch - 40);
            g.setFont(g.getFont().deriveFont(20f));
            g.drawString("Pressione ESC para voltar", cw - 125, ch + 20);
            g.setColor(new Color(255, 100, 100));
            g.drawString("Pressione Q para sair", cw - 105, ch + 60);
        }
        
        if (holeEffectAlpha > 0.01f) {
            g.setColor(new Color(140, 0, 0, (int)(holeEffectAlpha * 180)));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(g.getFont().deriveFont(Font.BOLD, Math.max(22f, (float)(getWidth() / 22.0))));
            g.setColor(new Color(255, 255, 255, (int)(holeEffectAlpha * 255)));
            String fallTxt = "VOCÊ CAIU NO ABISMO!";
            g.drawString(fallTxt, (getWidth() - g.getFontMetrics().stringWidth(fallTxt)) / 2, getHeight() / 2);
            holeEffectAlpha *= 0.88f;
        }
        
        bs.show();
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
                    // Lógica de corrida
                    if (camera.run && stamina > 0) {
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
