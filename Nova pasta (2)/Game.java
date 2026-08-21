import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
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

public class Game extends JFrame implements Runnable {
    
    private static final long serialVersionUID = 1L;
    private Thread thread;
    private boolean running;
    private BufferedImage image;
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
    
    private double startX = 1.5, startY = 1.5;

    public Game() {
        thread = new Thread(this);
        image = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
        camera = new Camera(startX, startY, 1, 0, 0, -0.66);
        
        try {
            cutsceneImg = javax.imageio.ImageIO.read(new java.io.File("1000132930.jpg"));
        } catch (Exception e) {
            System.out.println("Aviso: Imagem 1000132930.jpg não encontrada na pasta.");
        }
        
        texture = new Texture("wall.png", 128);
        monsterTexture = new Texture("1000132829-removebg-preview.png", 512);
        coinTexture = new Texture("Design sem nome.png", 256);
        portalTexture = new Texture("portal.png", 128);
        
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
        
        setSize(640, 480);
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setTitle("The Backrooms - Java 3D");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.black);
        setLocationRelativeTo(null);
        setVisible(true);
        
        centerX = getLocationOnScreen().x + getWidth() / 2;
        centerY = getLocationOnScreen().y + getHeight() / 2;
        
        start();
    }
    
    private void setupEntities() {
        entities.clear();
        monster = new Entity(24.5, 24.5, monsterTexture, 0);
        entities.add(monster);
        
        // Spawn 60 coins randomly
        int coinsSpawned = 0;
        int attempts = 0;
        while(coinsSpawned < 60 && attempts < 2000) {
            attempts++;
            int mx = (int)(Math.random() * Map.MAP_WIDTH);
            int my = (int)(Math.random() * Map.MAP_HEIGHT);
            // Dont spawn inside walls or right on the player (spawns closer now)
            if (Map.map[my][mx] == 0 && (mx > 2 || my > 2)) {
                entities.add(new Entity(mx + 0.5, my + 0.5, coinTexture, 1));
                coinsSpawned++;
            }
        }
    }
    
    private void teleportMonsterCloser() {
        int maxDist = Math.max(10, 25 - (robux / 3)); 
        int minDist = Math.max(8, 15 - (robux / 4));
        
        for (int attempt = 0; attempt < 100; attempt++) {
            int mx = (int)(Math.random() * Map.MAP_WIDTH);
            int my = (int)(Math.random() * Map.MAP_HEIGHT);
            double dx = mx - camera.xPos;
            double dy = my - camera.yPos;
            double dist = Math.sqrt(dx*dx + dy*dy);
            
            if (Map.map[my][mx] == 0 && dist >= minDist && dist <= maxDist) {
                monster.x = mx + 0.5;
                monster.y = my + 0.5;
                return;
            }
        }
    }
    
    public synchronized void start() {
        running = true;
        thread.start();
    }
    
    public synchronized void stop() {
        running = false;
        try { thread.join(); } catch(InterruptedException e) { e.printStackTrace(); }
    }
    
    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if(bs == null) { createBufferStrategy(3); return; }
        Graphics g = bs.getDrawGraphics();
        
        long elapsedCutscene = 0;
        if (cutsceneStartTime > 0) elapsedCutscene = System.currentTimeMillis() - cutsceneStartTime;
        
        if (inCutscene && elapsedCutscene < 12000) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            
            float fontSize = (float)(getWidth() / 25.0);
            g.setFont(g.getFont().deriveFont(fontSize));
            java.awt.FontMetrics fm = g.getFontMetrics();
            int cw = getWidth() / 2;
            int ch = getHeight() / 2;
            
            if (elapsedCutscene < 4000) {
                float alpha = 0;
                if (elapsedCutscene < 2000) alpha = (float)(elapsedCutscene / 2000.0);
                else alpha = (float)((4000 - elapsedCutscene) / 2000.0);
                if (alpha < 0) alpha = 0; if (alpha > 1) alpha = 1;
                g.setColor(new Color(1f, 1f, 1f, alpha));
                String text = "Voce nao deveria estar aqui...";
                g.drawString(text, cw - fm.stringWidth(text) / 2, ch);
            } else if (elapsedCutscene < 8000) {
                long e = elapsedCutscene - 4000;
                float alpha = 0;
                if (e < 2000) alpha = (float)(e / 2000.0);
                else alpha = (float)((4000 - e) / 2000.0);
                if (alpha < 0) alpha = 0; if (alpha > 1) alpha = 1;
                g.setColor(new Color(1f, 0f, 0f, alpha));
                String text = "CUIDADO";
                g.drawString(text, cw - fm.stringWidth(text) / 2, ch);
            } else {
                if (cutsceneImg != null) {
                    g.drawImage(cutsceneImg, 0, 0, getWidth(), getHeight(), null);
                    g.setColor(new Color(0, 0, 0, 180)); // Filtro escuro
                    g.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    g.setColor(Color.WHITE);
                    String text = "(Imagem 1000132930.jpg nao encontrada)";
                    g.drawString(text, cw - fm.stringWidth(text) / 2, ch);
                }
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
            g.setColor(new Color(150, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(32f));
            g.drawString("VOCE MORREU!", cw - 120, ch - 20);
            g.setFont(g.getFont().deriveFont(16f));
            g.drawString("Pressione R para reviver", cw - 90, ch + 20);
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
        
        if (inCutscene && elapsedCutscene >= 12000 && elapsedCutscene < 15000) {
            float alpha = 1.0f - ((elapsedCutscene - 12000) / 3000.0f);
            if (alpha < 0) alpha = 0; if (alpha > 1) alpha = 1;
            g.setColor(new Color(0, 0, 0, alpha));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        
        bs.show();
    }
    
    private void spawnPortal() {
        for (int attempt = 0; attempt < 100; attempt++) {
            int mx = (int)(Math.random() * Map.MAP_WIDTH);
            int my = (int)(Math.random() * Map.MAP_HEIGHT);
            double dx = mx - camera.xPos;
            double dy = my - camera.yPos;
            if (Map.map[my][mx] == 0 && (dx*dx + dy*dy > 100)) {
                entities.add(new Entity(mx + 0.5, my + 0.5, portalTexture, 2));
                return;
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
                
                if (camera.quit) System.exit(0);
                if (camera.esc && elapsed < 12000) {
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
            
            if (camera.quit) System.exit(0);
            
            // Revive
            if (health <= 0 && camera.revive) {
                health = 100;
                stamina = 100;
                camera.xPos = startX;
                camera.yPos = startY;
                monster.x = 24.5;
                monster.y = 24.5;
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
                            
                            if (robux == 50) {
                                spawnPortal();
                            }
                        } else if (e.type == 2 && dist < 0.8) { // Portal
                            won = true;
                        }
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
            if (robot != null && isFocused() && !paused && health > 0 && !won) {
                robot.mouseMove(centerX, centerY);
                camera.lastMouseX = centerX - getLocationOnScreen().x;
            }
        }
    }
    
    public static void main(String [] args) {
        new Game();
    }
}
