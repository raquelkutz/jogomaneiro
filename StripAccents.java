import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;

public class StripAccents {
    public static void main(String[] args) {
        String filePath = "JogoAudrey.java";
        Path path = Paths.get(filePath);

        try {
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

            content = stripAccents(content);

            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
            System.out.println("Accents stripped.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String stripAccents(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }
}
