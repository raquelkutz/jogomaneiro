import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * Banco de dados local para usuários e perfis do HobbyQuest.
 * Salva credenciais (com hash SHA-256) e dados de perfil (nome, avatar, foto, xp, metas)
 * em arquivo seguro de banco de dados 'hobbyquest_users.db'.
 */
public class BancoUsuarios {

    private static final String ARQUIVO_DB = "hobbyquest_users.db";

    public static class DadosUsuario {
        public String email;
        public String hashSenha;
        public String nome;
        public int avatar;
        public String caminhoFoto;
        public int totalXP;
        public int streakDias;
        public int totalDesafios;
        public int xpLeitura;
        public int nivelLeitura;
        public int xpArte;
        public int nivelArte;
        public int xpFitness;
        public int nivelFitness;

        public DadosUsuario() {
            this.nome = "Aventureiro";
            this.avatar = 0;
            this.caminhoFoto = "";
            this.totalXP = 25;
            this.streakDias = 1;
            this.totalDesafios = 0;
            this.xpLeitura = 15;
            this.nivelLeitura = 1;
            this.xpArte = 10;
            this.nivelArte = 1;
            this.xpFitness = 0;
            this.nivelFitness = 1;
        }
    }

    private static File getArquivo() {
        return new File(JogoAudrey.resolvePath(ARQUIVO_DB));
    }

    private static Properties carregarDb() {
        Properties p = new Properties();
        File f = getArquivo();
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                p.load(fis);
            } catch (Exception e) {
                System.err.println("Erro ao carregar banco de dados de usuarios: " + e.getMessage());
            }
        }
        return p;
    }

    private static void salvarDb(Properties p) {
        File f = getArquivo();
        try (FileOutputStream fos = new FileOutputStream(f)) {
            p.store(fos, "HobbyQuest Banco de Dados de Usuarios e Perfis");
        } catch (Exception e) {
            System.err.println("Erro ao salvar banco de dados de usuarios: " + e.getMessage());
        }
    }

    public static String hashSenha(String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(senha.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(senha.hashCode());
        }
    }

    public static boolean usuarioExiste(String email) {
        Properties p = carregarDb();
        String chave = "user." + email.trim().toLowerCase() + ".email";
        return p.containsKey(chave);
    }

    public static boolean cadastrarUsuario(String email, String senha, String nome) {
        if (usuarioExiste(email)) {
            return false;
        }
        Properties p = carregarDb();
        String e = email.trim().toLowerCase();
        String prefix = "user." + e + ".";

        p.setProperty(prefix + "email", e);
        p.setProperty(prefix + "hashSenha", hashSenha(senha));
        p.setProperty(prefix + "nome", nome.trim().isEmpty() ? (e.contains("@") ? e.substring(0, e.indexOf('@')) : e) : nome.trim());
        p.setProperty(prefix + "avatar", "0");
        p.setProperty(prefix + "caminhoFoto", "");
        p.setProperty(prefix + "totalXP", "25");
        p.setProperty(prefix + "streakDias", "1");
        p.setProperty(prefix + "totalDesafios", "0");
        p.setProperty(prefix + "xpLeitura", "15");
        p.setProperty(prefix + "nivelLeitura", "1");
        p.setProperty(prefix + "xpArte", "10");
        p.setProperty(prefix + "nivelArte", "1");
        p.setProperty(prefix + "xpFitness", "0");
        p.setProperty(prefix + "nivelFitness", "1");

        salvarDb(p);
        return true;
    }

    public static DadosUsuario autenticar(String email, String senha) {
        Properties p = carregarDb();
        String e = email.trim().toLowerCase();
        String prefix = "user." + e + ".";

        if (!p.containsKey(prefix + "email")) {
            return null;
        }

        String hashArmazenado = p.getProperty(prefix + "hashSenha", "");
        String hashDigitado = hashSenha(senha);

        if (!hashArmazenado.equals(hashDigitado)) {
            return null;
        }

        DadosUsuario dados = new DadosUsuario();
        dados.email = e;
        dados.hashSenha = hashArmazenado;
        dados.nome = p.getProperty(prefix + "nome", e.contains("@") ? e.substring(0, e.indexOf('@')) : e);
        dados.avatar = Integer.parseInt(p.getProperty(prefix + "avatar", "0"));
        dados.caminhoFoto = p.getProperty(prefix + "caminhoFoto", "");
        dados.totalXP = Integer.parseInt(p.getProperty(prefix + "totalXP", "25"));
        dados.streakDias = Integer.parseInt(p.getProperty(prefix + "streakDias", "1"));
        dados.totalDesafios = Integer.parseInt(p.getProperty(prefix + "totalDesafios", "0"));
        dados.xpLeitura = Integer.parseInt(p.getProperty(prefix + "xpLeitura", "15"));
        dados.nivelLeitura = Integer.parseInt(p.getProperty(prefix + "nivelLeitura", "1"));
        dados.xpArte = Integer.parseInt(p.getProperty(prefix + "xpArte", "10"));
        dados.nivelArte = Integer.parseInt(p.getProperty(prefix + "nivelArte", "1"));
        dados.xpFitness = Integer.parseInt(p.getProperty(prefix + "nivelFitness", "0"));
        dados.nivelFitness = Integer.parseInt(p.getProperty(prefix + "nivelFitness", "1"));

        return dados;
    }

    public static void atualizarPerfil(String email, String novoNome, int avatar, String caminhoFoto) {
        Properties p = carregarDb();
        String e = email.trim().toLowerCase();
        String prefix = "user." + e + ".";

        if (p.containsKey(prefix + "email")) {
            p.setProperty(prefix + "nome", novoNome);
            p.setProperty(prefix + "avatar", String.valueOf(avatar));
            p.setProperty(prefix + "caminhoFoto", caminhoFoto != null ? caminhoFoto : "");
            salvarDb(p);
        }
    }

    public static void salvarProgresso(String email, int totalXP, int streakDias, int totalDesafios,
                                       int xpLeitura, int nivelLeitura,
                                       int xpArte, int nivelArte,
                                       int xpFitness, int nivelFitness) {
        Properties p = carregarDb();
        String e = email.trim().toLowerCase();
        String prefix = "user." + e + ".";

        if (p.containsKey(prefix + "email")) {
            p.setProperty(prefix + "totalXP", String.valueOf(totalXP));
            p.setProperty(prefix + "streakDias", String.valueOf(streakDias));
            p.setProperty(prefix + "totalDesafios", String.valueOf(totalDesafios));
            p.setProperty(prefix + "xpLeitura", String.valueOf(xpLeitura));
            p.setProperty(prefix + "nivelLeitura", String.valueOf(nivelLeitura));
            p.setProperty(prefix + "xpArte", String.valueOf(xpArte));
            p.setProperty(prefix + "nivelArte", String.valueOf(nivelArte));
            p.setProperty(prefix + "xpFitness", String.valueOf(xpFitness));
            p.setProperty(prefix + "nivelFitness", String.valueOf(nivelFitness));
            salvarDb(p);
        }
    }
}
