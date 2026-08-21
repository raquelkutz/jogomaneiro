import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;

public class Camera implements KeyListener, MouseMotionListener {
    public double xPos, yPos, xDir, yDir, xPlane, yPlane;
    public boolean left, right, forward, back;
    public boolean esc, quit, run, revive;
    public double MOVE_SPEED = 0.04;
    public final double ROT_SPEED = 0.03;
    public final double MOUSE_SENSITIVITY = 0.002;
    
    public int lastMouseX = -1;

    public Camera(double x, double y, double xd, double yd, double xp, double yp) {
        xPos = x;
        yPos = y;
        xDir = xd;
        yDir = yd;
        xPlane = xp;
        yPlane = yp;
    }

    public void update() {
        double buffer = 0.3;
        if(forward) {
            if(Map.map[(int)(yPos)][(int)(xPos + xDir * MOVE_SPEED + xDir * buffer)] == 0) xPos += xDir * MOVE_SPEED;
            if(Map.map[(int)(yPos + yDir * MOVE_SPEED + yDir * buffer)][(int)xPos] == 0) yPos += yDir * MOVE_SPEED;
        }
        if(back) {
            if(Map.map[(int)(yPos)][(int)(xPos - xDir * MOVE_SPEED - xDir * buffer)] == 0) xPos -= xDir * MOVE_SPEED;
            if(Map.map[(int)(yPos - yDir * MOVE_SPEED - yDir * buffer)][(int)xPos] == 0) yPos -= yDir * MOVE_SPEED;
        }
        if(right) rotate(-ROT_SPEED);
        if(left)  rotate(ROT_SPEED);
    }
    
    public void rotate(double angle) {
        double oldxDir = xDir;
        xDir = xDir * Math.cos(angle) - yDir * Math.sin(angle);
        yDir = oldxDir * Math.sin(angle) + yDir * Math.cos(angle);
        double oldxPlane = xPlane;
        xPlane = xPlane * Math.cos(angle) - yPlane * Math.sin(angle);
        yPlane = oldxPlane * Math.sin(angle) + yPlane * Math.cos(angle);
    }

    @Override
    public void keyPressed(KeyEvent key) {
        if(key.getKeyCode() == KeyEvent.VK_LEFT  || key.getKeyCode() == KeyEvent.VK_A) left = true;
        if(key.getKeyCode() == KeyEvent.VK_RIGHT || key.getKeyCode() == KeyEvent.VK_D) right = true;
        if(key.getKeyCode() == KeyEvent.VK_UP    || key.getKeyCode() == KeyEvent.VK_W) forward = true;
        if(key.getKeyCode() == KeyEvent.VK_DOWN  || key.getKeyCode() == KeyEvent.VK_S) back = true;
        if(key.getKeyCode() == KeyEvent.VK_ESCAPE) esc = true;
        if(key.getKeyCode() == KeyEvent.VK_Q) quit = true;
        if(key.getKeyCode() == KeyEvent.VK_SHIFT) run = true;
        if(key.getKeyCode() == KeyEvent.VK_R) revive = true;
    }

    @Override
    public void keyReleased(KeyEvent key) {
        if(key.getKeyCode() == KeyEvent.VK_LEFT  || key.getKeyCode() == KeyEvent.VK_A) left = false;
        if(key.getKeyCode() == KeyEvent.VK_RIGHT || key.getKeyCode() == KeyEvent.VK_D) right = false;
        if(key.getKeyCode() == KeyEvent.VK_UP    || key.getKeyCode() == KeyEvent.VK_W) forward = false;
        if(key.getKeyCode() == KeyEvent.VK_DOWN  || key.getKeyCode() == KeyEvent.VK_S) back = false;
        if(key.getKeyCode() == KeyEvent.VK_ESCAPE) esc = false;
        if(key.getKeyCode() == KeyEvent.VK_Q) quit = false;
        if(key.getKeyCode() == KeyEvent.VK_SHIFT) run = false;
        if(key.getKeyCode() == KeyEvent.VK_R) revive = false;
    }

    @Override
    public void keyTyped(KeyEvent arg0) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        if (lastMouseX == -1) { lastMouseX = e.getX(); return; }
        int dx = e.getX() - lastMouseX;
        lastMouseX = e.getX();
        if (dx != 0) rotate(-dx * MOUSE_SENSITIVITY);
    }

    @Override
    public void mouseDragged(MouseEvent e) { mouseMoved(e); }
}
