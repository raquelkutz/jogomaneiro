import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Database {
    private static String getArquivoSave(int slot) {
        return "savegame" + slot + ".db";
    }

    public static boolean saveExiste(int slot) {
        return new File(getArquivoSave(slot)).exists();
    }

    public static boolean apagarSave(int slot) {
        File f = new File(getArquivoSave(slot));
        if (f.exists()) {
            return f.delete();
        }
        return false;
    }

    public static void salvarEstado(int slot, Properties props) {
        try (FileOutputStream fos = new FileOutputStream(getArquivoSave(slot))) {
            props.store(fos, "Audrey Adventure Save Game Slot " + slot);
            System.out.println("Jogo salvo com sucesso no slot " + slot + ".");
        } catch (Exception e) {
            System.err.println("Erro ao salvar o jogo: " + e.getMessage());
        }
    }

    public static Properties carregarEstado(int slot) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(getArquivoSave(slot))) {
            props.load(fis);
            return props;
        } catch (Exception e) {
            System.err.println("Erro ao carregar o jogo: " + e.getMessage());
            return null;
        }
    }
}
