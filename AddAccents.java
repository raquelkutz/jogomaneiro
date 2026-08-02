import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class AddAccents {
    public static void main(String[] args) {
        String filePath = "JogoAudrey.java";
        Path path = Paths.get(filePath);

        try {
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

            Map<String, String> replacements = new LinkedHashMap<>();
            replacements.put("\\bCONFIGURACOES\\b", "CONFIGURAÇÕES");
            replacements.put("\\bINVENTARIO\\b", "INVENTÁRIO");
            replacements.put("\\bDIARIO\\b", "DIÁRIO");
            replacements.put("\\bDialogo\\b", "Diálogo");
            replacements.put("\\bDialogos\\b", "Diálogos");
            replacements.put("\\bInventario\\b", "Inventário");
            replacements.put("\\bDiario\\b", "Diário");
            replacements.put("\\bOpcoes\\b", "Opções");
            replacements.put("\\bOPCOES\\b", "OPÇÕES");
            replacements.put("\\bcapitulo\\b", "capítulo");
            replacements.put("\\bconcluido\\b", "concluído");
            replacements.put("\\bIncrivel\\b", "Incrível");
            replacements.put("\\besta\\b", "está");
            replacements.put("\\bEsta\\b", "Está");
            replacements.put("\\bVa\\b", "Vá");
            replacements.put("\\bne\\?\\b", "né?");
            replacements.put("\\bai!\\b", "aí!");
            replacements.put("\\bexpressao\\b", "expressão");
            replacements.put("\\bpratica\\b", "prática");
            replacements.put("\\bdiaria\\b", "diária");
            replacements.put("\\besbocos\\b", "esboços");
            replacements.put("\\btraco\\b", "traço");
            replacements.put("\\bmagnifico\\b", "magnífico");
            replacements.put("\\bFaca\\b", "Faça");
            replacements.put("\\bParabens\\b", "Parabéns");
            replacements.put("\\bnao\\b", "não");
            replacements.put("\\bNao\\b", "Não");
            replacements.put("\\bvoce\\b", "você");
            replacements.put("\\bVoce\\b", "Você");
            replacements.put("\\bja\\b", "já");
            replacements.put("\\bJa\\b", "Já");
            replacements.put("\\bcoordenacao\\b", "coordenação");
            replacements.put("\\bArmario\\b", "Armário");
            replacements.put("\\barmario\\b", "armário");
            replacements.put("\\bNivel\\b", "Nível");
            replacements.put("\\bMissoes\\b", "Missões");
            replacements.put("\\bmissoes\\b", "missões");
            replacements.put("\\bproprias\\b", "próprias");
            replacements.put("\\bproxima\\b", "próxima");
            replacements.put("\\bso\\b", "só");
            replacements.put("\\bvoces\\b", "vocês");
            replacements.put("\\bVoces\\b", "Vocês");
            replacements.put("Qualquer coisa e ", "Qualquer coisa é ");
            replacements.put("a chave de ouro e ", "a chave de ouro é ");
            replacements.put("a verdadeira expressao e ", "a verdadeira expressão é ");
            replacements.put("O segredo da verdadeira expressao e ", "O segredo da verdadeira expressão é ");
            replacements.put(" e logo ali", " é logo ali");

            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                content = content.replaceAll(entry.getKey(), entry.getValue());
            }

            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
            System.out.println("Accents added.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
