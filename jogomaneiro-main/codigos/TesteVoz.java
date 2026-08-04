import java.io.File;
import javax.sound.sampled.*;

public class TesteVoz {
    public static void main(String[] args) throws Exception {
        String path = JogoAudrey.resolvePath("vozdopersonagemnicollas.wav");
        File f = new File(path);
        if (!f.exists()) {
            f = new File(JogoAudrey.resolvePath("voz_nicolas.wav"));
        }
        System.out.println("existe: " + f.exists() + " tamanho: " + f.length());
        AudioInputStream in = AudioSystem.getAudioInputStream(f);
        System.out.println("formato: " + in.getFormat());
        byte[] buffer = new byte[8192];
        int n;
        long total = 0;
        while ((n = in.read(buffer)) != -1) {
            total += n;
        }
        System.out.println("bytes lidos: " + total);
    }
}
