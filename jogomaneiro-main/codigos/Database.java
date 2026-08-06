import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Database {
    private static String getArquivoSave(int slot) {
        return JogoAudrey.resolvePath("savegame" + slot + ".db");
    }

    public static boolean saveExiste(int slot) {
        return new File(getArquivoSave(slot)).exists();
    }

    public static boolean apagarSave(int slot) {
        String filename = "savegame" + slot + ".db";
        boolean deletedAny = false;

        // 1. Delete from classDir subfolders
        try {
            java.io.File classDir = new java.io.File(JogoAudrey.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (classDir.isFile()) {
                classDir = classDir.getParentFile();
            }
            String[] subfolders = {"", "imagens/", "audios/", "codigos/"};
            for (String sub : subfolders) {
                File f = new File(classDir, sub + filename);
                if (f.exists()) {
                    if (f.delete()) deletedAny = true;
                }
            }
        } catch (Exception e) {
            // Ignore
        }

        // 2. Delete from CWD and CWD subfolders
        String[] subfolders = {"", "imagens/", "audios/", "codigos/"};
        for (String sub : subfolders) {
            File f = new File(sub + filename);
            if (f.exists()) {
                if (f.delete()) deletedAny = true;
            }
        }

        // 3. Delete from jogomaneiro-main and subfolders
        for (String sub : subfolders) {
            File f = new File("jogomaneiro-main/" + sub + filename);
            if (f.exists()) {
                if (f.delete()) deletedAny = true;
            }
        }

        // 4. Fallback directly resolved path
        File fallbackFile = new File(getArquivoSave(slot));
        if (fallbackFile.exists()) {
            if (fallbackFile.delete()) deletedAny = true;
        }

        return deletedAny;
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
