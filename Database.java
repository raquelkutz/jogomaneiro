import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Database {
    private static final String ARQUIVO_SAVE = "savegame.db";

    public static boolean saveExiste() {
        return new File(ARQUIVO_SAVE).exists();
    }

    public static void salvarEstado(int indiceMapa, int audreyX, boolean temChave, boolean armarioEstaAberto, 
                                    boolean nicolasJaFoiEncontrado, boolean livroJoiFoiPego, 
                                    boolean temLivro, int faseDialogoNicolas) {
        Properties props = new Properties();
        
        props.setProperty("indiceMapa", String.valueOf(indiceMapa));
        props.setProperty("audreyX", String.valueOf(audreyX));
        props.setProperty("temChave", String.valueOf(temChave));
        props.setProperty("armarioEstaAberto", String.valueOf(armarioEstaAberto));
        props.setProperty("nicolasJaFoiEncontrado", String.valueOf(nicolasJaFoiEncontrado));
        props.setProperty("livroJoiFoiPego", String.valueOf(livroJoiFoiPego));
        props.setProperty("temLivro", String.valueOf(temLivro));
        props.setProperty("faseDialogoNicolas", String.valueOf(faseDialogoNicolas));

        try (FileOutputStream fos = new FileOutputStream(ARQUIVO_SAVE)) {
            props.store(fos, "Audrey Adventure Save Game");
            System.out.println("Jogo salvo com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro ao salvar o jogo: " + e.getMessage());
        }
    }

    public static Properties carregarEstado() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(ARQUIVO_SAVE)) {
            props.load(fis);
            return props;
        } catch (Exception e) {
            System.err.println("Erro ao carregar o jogo: " + e.getMessage());
            return null;
        }
    }
}
