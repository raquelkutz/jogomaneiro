import java.awt.Color;

public class Screen {
    public int[][] map;
    public int width, height;
    public Texture texture;
    public double[] zBuffer;

    public Screen(int[][] m, int w, int h, Texture tex) {
        map = m;
        width = w;
        height = h;
        texture = tex;
        zBuffer = new double[width];
    }

    public void update(Camera camera, int[] pixels) {
        // Renderização do teto e chão via Floor Casting para ter perspectiva
        for(int y = 0; y < height; y++) {
            float rayDirX0 = (float)(camera.xDir - camera.xPlane);
            float rayDirY0 = (float)(camera.yDir - camera.yPlane);
            float rayDirX1 = (float)(camera.xDir + camera.xPlane);
            float rayDirY1 = (float)(camera.yDir + camera.yPlane);

            int p = y - height / 2;
            boolean isFloor = true;
            if (p < 0) {
                isFloor = false;
                p = -p; 
            }
            if (p == 0) p = 1;

            float posZ = 0.5f * height;
            float rowDistance = posZ / p;

            float floorStepX = rowDistance * (rayDirX1 - rayDirX0) / width;
            float floorStepY = rowDistance * (rayDirY1 - rayDirY0) / width;

            float floorX = (float)camera.xPos + rowDistance * rayDirX0;
            float floorY = (float)camera.yPos + rowDistance * rayDirY0;

            for(int x = 0; x < width; x++) {
                int cellX = (int)(floorX);
                int cellY = (int)(floorY);
                float tx = floorX - cellX;
                float ty = floorY - cellY;
                
                int color;
                if (!isFloor) {
                    color = new Color(60, 50, 20).getRGB(); // Teto mais claro (amarelado escuro)
                    // Painéis de luz fluorescente amarela - maiores e mais brilhantes!
                    if (tx > 0.25 && tx < 0.75 && ty > 0.2 && ty < 0.8) {
                        if (cellX % 2 == 0 && cellY % 2 == 0) {
                            color = new Color(255, 240, 140).getRGB(); // Luz amarela brilhante
                        }
                    }
                } else {
                    color = new Color(55, 48, 25).getRGB(); // Chão mais claro e amarelado
                }

                // Fog mais suave no teto e chão (alcance maior = mais luz)
                double fogRatio = Math.min(1.0, rowDistance / 10.0);
                // Adicionar tom amarelado ao fog (não ir totalmente para preto)
                int fogR = 30, fogG = 25, fogB = 5; // Cor da névoa amarelada
                int r = (color >> 16) & 255;
                int g = (color >> 8) & 255;
                int b = color & 255;
                r = (int)(r * (1 - fogRatio) + fogR * fogRatio);
                g = (int)(g * (1 - fogRatio) + fogG * fogRatio);
                b = (int)(b * (1 - fogRatio) + fogB * fogRatio);
                
                pixels[x + y * width] = new Color(r,g,b).getRGB();

                floorX += floorStepX;
                floorY += floorStepY;
            }
        }

        for (int x = 0; x < width; x++) {
            double cameraX = 2 * x / (double) (width) - 1;
            double rayDirX = camera.xDir + camera.xPlane * cameraX;
            double rayDirY = camera.yDir + camera.yPlane * cameraX;

            int mapX = (int) camera.xPos;
            int mapY = (int) camera.yPos;

            double sideDistX;
            double sideDistY;

            double deltaDistX = Math.abs(1 / rayDirX);
            double deltaDistY = Math.abs(1 / rayDirY);
            double perpWallDist;

            int stepX, stepY;
            boolean hit = false;
            int side = 0;

            if (rayDirX < 0) {
                stepX = -1;
                sideDistX = (camera.xPos - mapX) * deltaDistX;
            } else {
                stepX = 1;
                sideDistX = (mapX + 1.0 - camera.xPos) * deltaDistX;
            }
            if (rayDirY < 0) {
                stepY = -1;
                sideDistY = (camera.yPos - mapY) * deltaDistY;
            } else {
                stepY = 1;
                sideDistY = (mapY + 1.0 - camera.yPos) * deltaDistY;
            }

            while (!hit) {
                if (sideDistX < sideDistY) {
                    sideDistX += deltaDistX;
                    mapX += stepX;
                    side = 0;
                } else {
                    sideDistY += deltaDistY;
                    mapY += stepY;
                    side = 1;
                }
                if (mapX >= Map.MAP_WIDTH || mapY >= Map.MAP_HEIGHT || mapX < 0 || mapY < 0) {
                    hit = true;
                } else if (map[mapY][mapX] > 0) {
                    hit = true;
                }
            }

            if (side == 0)
                perpWallDist = (mapX - camera.xPos + (1 - stepX) / 2) / rayDirX;
            else
                perpWallDist = (mapY - camera.yPos + (1 - stepY) / 2) / rayDirY;

            int lineHeight = (int) (height / perpWallDist);

            int drawStart = -lineHeight / 2 + height / 2;
            if (drawStart < 0) drawStart = 0;

            int drawEnd = lineHeight / 2 + height / 2;
            if (drawEnd >= height) drawEnd = height - 1;

            // Salvar a distância da parede no ZBuffer para ocultar sprites depois
            zBuffer[x] = perpWallDist;

            // Calcular X da textura
            double wallX;
            if (side == 0) wallX = camera.yPos + perpWallDist * rayDirY;
            else           wallX = camera.xPos + perpWallDist * rayDirX;
            wallX -= Math.floor(wallX);

            double texXDouble = wallX * (double)texture.SIZE;
            if(side == 0 && rayDirX > 0) texXDouble = texture.SIZE - texXDouble - 1;
            if(side == 1 && rayDirY < 0) texXDouble = texture.SIZE - texXDouble - 1;
            int texX = (int)texXDouble;
            if (texX < 0) texX = 0;
            if (texX >= texture.SIZE) texX = texture.SIZE - 1;
            double texXfrac = texXDouble - texX;
            // Limitar texXfrac para que texX+1 não saia dos limites
            int texX1 = Math.min(texX + 1, texture.SIZE - 1);

            for (int y = drawStart; y < drawEnd; y++) {
                int texYFull = ((y * 256 - height * 128 + lineHeight * 128) * texture.SIZE) / lineHeight;
                int texY = (texYFull / 256);
                double texYfrac = (texYFull & 255) / 256.0;
                if (texY < 0) { texY = 0; texYfrac = 0; }
                if (texY >= texture.SIZE - 1) { texY = texture.SIZE - 2; texYfrac = 1.0; }
                int texY1 = Math.min(texY + 1, texture.SIZE - 1);
                
                int c00 = texture.pixels[texX  + (texY  * texture.SIZE)];
                int c10 = texture.pixels[texX1 + (texY  * texture.SIZE)];
                int c01 = texture.pixels[texX  + (texY1 * texture.SIZE)];
                int c11 = texture.pixels[texX1 + (texY1 * texture.SIZE)];
                
                int r00 = (c00 >> 16) & 255, g00 = (c00 >> 8) & 255, b00 = c00 & 255;
                int r10 = (c10 >> 16) & 255, g10 = (c10 >> 8) & 255, b10 = c10 & 255;
                int r01 = (c01 >> 16) & 255, g01 = (c01 >> 8) & 255, b01 = c01 & 255;
                int r11 = (c11 >> 16) & 255, g11 = (c11 >> 8) & 255, b11 = c11 & 255;
                
                int r = (int)(r00 * (1 - texXfrac) * (1 - texYfrac) + r10 * texXfrac * (1 - texYfrac) + r01 * (1 - texXfrac) * texYfrac + r11 * texXfrac * texYfrac);
                int g = (int)(g00 * (1 - texXfrac) * (1 - texYfrac) + g10 * texXfrac * (1 - texYfrac) + g01 * (1 - texXfrac) * texYfrac + g11 * texXfrac * texYfrac);
                int b = (int)(b00 * (1 - texXfrac) * (1 - texYfrac) + b10 * texXfrac * (1 - texYfrac) + b01 * (1 - texXfrac) * texYfrac + b11 * texXfrac * texYfrac);
                if (side == 1) {
                    r = (r >> 1);
                    g = (g >> 1);
                    b = (b >> 1);
                }
                
                // Fog nas paredes com tom amarelado
                double fogRatio = Math.min(1.0, perpWallDist / 10.0);
                int fogR = 30, fogG = 25, fogB = 5;
                r = (int)(r * (1 - fogRatio) + fogR * fogRatio);
                g = (int)(g * (1 - fogRatio) + fogG * fogRatio);
                b = (int)(b * (1 - fogRatio) + fogB * fogRatio);
                
                pixels[x + y * width] = (r << 16) | (g << 8) | b;
            }
        }
        
        // Vinheta mais suave (escurece só as bordas, não o centro)
        for(int y=0; y<height; y++) {
            for(int x=0; x<width; x++) {
                double dist = Math.sqrt((x-width/2.0)*(x-width/2.0) + (y-height/2.0)*(y-height/2.0));
                if (dist > height/1.2) { // Borda mais recuada
                    double dark = Math.min(0.7, (dist - height/1.2)/(height/3.0)); // max 70% dark
                    int p = pixels[x + y*width];
                    int r = (p >> 16) & 255;
                    int g = (p >> 8) & 255;
                    int b = p & 255;
                    r = (int)(r * (1-dark));
                    g = (int)(g * (1-dark));
                    b = (int)(b * (1-dark));
                    pixels[x + y*width] = (r << 16) | (g << 8) | b;
                }
            }
        }
    }
    
    // Método para renderizar sprites (O monstro)
    public void renderSprite(Camera camera, Entity entity, int[] pixels) {
        double spriteX = entity.x - camera.xPos;
        double spriteY = entity.y - camera.yPos;

        // Transformar com a matriz inversa da câmera
        double invDet = 1.0 / (camera.xPlane * camera.yDir - camera.xDir * camera.yPlane);
        double transformX = invDet * (camera.yDir * spriteX - camera.xDir * spriteY);
        double transformY = invDet * (-camera.yPlane * spriteX + camera.xPlane * spriteY); // Depth

        if (transformY > 0) { // Se o monstro estiver na frente da câmera
            int spriteScreenX = (int)((width / 2) * (1 + transformX / transformY));

            double scale = (entity.type == 1) ? 0.6 : 1.0;
            // Desloca a moeda para baixo para encostar no chão (metade da diferença de tamanho)
            int vMoveScreen = (entity.type == 1) ? (int)((height / transformY) * 0.20) : 0;

            int spriteHeight = Math.abs((int)((height / transformY) * scale)); // Calcular altura
            int drawStartY = -spriteHeight / 2 + height / 2 + vMoveScreen;
            if (drawStartY < 0) drawStartY = 0;
            int drawEndY = spriteHeight / 2 + height / 2 + vMoveScreen;
            if (drawEndY >= height) drawEndY = height - 1;

            int spriteWidth = Math.abs((int)((height / transformY) * scale)); // Calcular largura
            int drawStartX = -spriteWidth / 2 + spriteScreenX;
            if (drawStartX < 0) drawStartX = 0;
            int drawEndX = spriteWidth / 2 + spriteScreenX;
            if (drawEndX >= width) drawEndX = width - 1;

            for (int stripe = drawStartX; stripe < drawEndX; stripe++) {
                double texXDouble = 256.0 * (stripe - (-spriteWidth / 2 + spriteScreenX)) * entity.texture.SIZE / spriteWidth;
                int texX = (int)(texXDouble / 256.0);
                double texXfrac = (texXDouble / 256.0) - texX;
                if (texX < 0) { texX = 0; texXfrac = 0; }
                if (texX >= entity.texture.SIZE - 1) { texX = entity.texture.SIZE - 2; texXfrac = 1.0; }
                // Verificar Z-Buffer: desenhar apenas se estiver mais perto que a parede
                if (transformY > 0 && stripe > 0 && stripe < width && transformY < zBuffer[stripe]) {
                    for (int y = drawStartY; y < drawEndY; y++) {
                        int d = (y - vMoveScreen) * 256 - height * 128 + spriteHeight * 128;
                        int texYFull = (d * entity.texture.SIZE) / spriteHeight;
                        int texY = texYFull / 256;
                        double texYfrac = (texYFull & 255) / 256.0;
                        if (texY < 0) { texY = 0; texYfrac = 0; }
                        if (texY >= entity.texture.SIZE - 1) { texY = entity.texture.SIZE - 2; texYfrac = 1.0; }
                        
                        int c00 = entity.texture.pixels[texX + texY * entity.texture.SIZE];
                        int c10 = entity.texture.pixels[(texX + 1) + texY * entity.texture.SIZE];
                        int c01 = entity.texture.pixels[texX + (texY + 1) * entity.texture.SIZE];
                        int c11 = entity.texture.pixels[(texX + 1) + (texY + 1) * entity.texture.SIZE];
                        
                        int r00 = (c00 >> 16) & 255, g00 = (c00 >> 8) & 255, b00 = c00 & 255;
                        int r10 = (c10 >> 16) & 255, g10 = (c10 >> 8) & 255, b10 = c10 & 255;
                        int r01 = (c01 >> 16) & 255, g01 = (c01 >> 8) & 255, b01 = c01 & 255;
                        int r11 = (c11 >> 16) & 255, g11 = (c11 >> 8) & 255, b11 = c11 & 255;
                        
                        int r = (int)(r00 * (1 - texXfrac) * (1 - texYfrac) + r10 * texXfrac * (1 - texYfrac) + r01 * (1 - texXfrac) * texYfrac + r11 * texXfrac * texYfrac);
                        int g = (int)(g00 * (1 - texXfrac) * (1 - texYfrac) + g10 * texXfrac * (1 - texYfrac) + g01 * (1 - texXfrac) * texYfrac + g11 * texXfrac * texYfrac);
                        int b = (int)(b00 * (1 - texXfrac) * (1 - texYfrac) + b10 * texXfrac * (1 - texYfrac) + b01 * (1 - texXfrac) * texYfrac + b11 * texXfrac * texYfrac);
                        int color = (r << 16) | (g << 8) | b;
                        
                        // Usar o canal alpha real para transparência
                        // Pega alpha do pixel original (antes do bilinear)
                        int alpha = (c00 >> 24) & 0xFF;
                        if (alpha > 128) { // Pixel opaco suficiente para desenhar
                            // Fog
                            double fogRatio = Math.min(1.0, transformY / 10.0);
                            int fogR = 30, fogG = 25, fogB = 5;
                            r = (int)(r * (1 - fogRatio) + fogR * fogRatio);
                            g = (int)(g * (1 - fogRatio) + fogG * fogRatio);
                            b = (int)(b * (1 - fogRatio) + fogB * fogRatio);
                            pixels[stripe + y * width] = new Color(r,g,b).getRGB();
                        }
                    }
                }
            }
        }
    }
}
