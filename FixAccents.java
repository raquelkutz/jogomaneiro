import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class FixAccents {
    public static void main(String[] args) {
        String filePath = "JogoAudrey.java";
        Path path = Paths.get(filePath);

        try {
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

            Map<String, String> replacements = new LinkedHashMap<>();
            replacements.put("CONFIGURACOES", "CONFIGURAÇÕES");
            replacements.put("INVENTARIO", "INVENTÁRIO");
            replacements.put("DIARIO", "DIÁRIO");
            replacements.put("Dialogo", "Diálogo");
            replacements.put("Dialogos", "Diálogos");
            replacements.put("Inventario", "Inventário");
            replacements.put("Diario", "Diário");
            replacements.put("Opcoes", "Opções");
            replacements.put("OPCOES", "OPÇÕES");
            replacements.put("captulo", "capítulo");
            replacements.put("concludo", "concluído");
            replacements.put("Incrvel", "Incrível");
            replacements.put("est\ufffd", "está");
            replacements.put("V\ufffd ", "Vá ");
            replacements.put(" \ufffd ", " é ");
            replacements.put("n\ufffd?", "né?");
            replacements.put("a\ufffd!", "aí!");
            replacements.put("expresso", "expressão");
            replacements.put("prtica", "prática");
            replacements.put("diria", "diária");
            replacements.put("esboos", "esboços");
            replacements.put("trao", "traço");
            replacements.put("magnfico", "magnífico");
            replacements.put("Faa", "Faça");
            replacements.put("Parabns", "Parabéns");
            replacements.put("n\ufffdo", "não");
            replacements.put("N\ufffdo", "Não");
            replacements.put("voc\ufffd", "você");
            replacements.put("Voc\ufffd", "Você");
            replacements.put("j\ufffd", "já");
            replacements.put("J\ufffd", "Já");
            replacements.put("coordena\ufffdo", "coordenação");
            replacements.put("Arm\ufffdrio", "Armário");
            replacements.put("arm\ufffdrio", "armário");
            replacements.put("Di\ufffdrio", "Diário");
            replacements.put("N\ufffdvel", "Nível");
            replacements.put("Miss\ufffdes", "Missões");
            replacements.put("miss\ufffdes", "missões");
            replacements.put("pr\ufffdprias", "próprias");
            replacements.put("pr\ufffdxima", "próxima");
            replacements.put(" s\ufffd ", " só ");
            replacements.put("cap\ufffdtulo", "capítulo");
            replacements.put("conclu\ufffddo", "concluído");
            replacements.put("Incr\ufffdvel", "Incrível");
            replacements.put("express\ufffdo", "expressão");
            replacements.put("pr\ufffdtica", "prática");
            replacements.put("di\ufffdria", "diária");
            replacements.put("esbo\ufffdos", "esboços");
            replacements.put("tra\ufffdo", "traço");
            replacements.put("magn\ufffdfico", "magnífico");
            replacements.put("Fa\ufffda", "Faça");
            replacements.put("Parab\ufffdns", "Parabéns");
            replacements.put(" voc\ufffdes ", " vocês ");
            replacements.put("Voc\ufffdes ", "Vocês ");

            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                content = content.replace(entry.getKey(), entry.getValue());
            }

            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
            System.out.println("Accents fixed via string replace.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
