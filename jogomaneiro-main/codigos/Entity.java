public class Entity {
    public double x, y;
    public Texture texture;
    public double speed = 0.02;
    public int type = 0; // 0=Monster, 1=Coin, 2=Portal
    public boolean active = true;
    
    public Entity(double x, double y, Texture texture, int type) {
        this.x = x;
        this.y = y;
        this.texture = texture;
        this.type = type;
        if (type == 0) speed = 0.02; // Monstro inicia mais devagar
    }
    
    public void update(Camera player) {
        if (!active || type != 0) return; // Só monstro se move
        
        // IA do monstro: Move na direção do jogador
        double dx = player.xPos - this.x;
        double dy = player.yPos - this.y;
        double dist = Math.sqrt(dx*dx + dy*dy);
        
        if (dist > 0.5) { // Para se estiver colado
            double moveX = (dx / dist) * speed;
            double moveY = (dy / dist) * speed;
            
            // Colisão com paredes
            if(Map.map[(int)y][(int)(x + moveX)] == 0) x += moveX;
            if(Map.map[(int)(y + moveY)][(int)x] == 0) y += moveY;
        }
    }
}
