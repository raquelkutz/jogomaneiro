import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.awt.Desktop;

public class ServidorIntegradoJava {

    private static final int PORT = 3000;
    private static HttpServer server = null;
    private static final List<HttpExchange> sseClients = new CopyOnWriteArrayList<>();
    private static boolean iniciado = false;

    public static synchronized void iniciar() {
        if (iniciado) return;
        new Thread(() -> {
            try {
                server = HttpServer.create(new InetSocketAddress(PORT), 0);

                // Configura rotas da API
                server.createContext("/api/iniciar-jogo", new IniciarJogoHandler());
                server.createContext("/api/status-jogo", new StatusJogoHandler());
                server.createContext("/api/evento-jogo", new EventoJogoHandler());
                server.createContext("/api/eventos", new SseHandler());

                // Servidor de arquivos estáticos do TCC (novoprojeto_tcc)
                server.createContext("/", new ArquivosEstaticosHandler());

                server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
                server.start();
                iniciado = true;
                System.out.println("[ServidorIntegradoJava] ✨ Servidor HTTP do TCC iniciado na porta " + PORT + "!");
                System.out.println("[ServidorIntegradoJava] 👉 App disponível em http://localhost:" + PORT + "/home.html");
            } catch (Exception e) {
                System.out.println("[ServidorIntegradoJava] Porta " + PORT + " ocupada ou servidor já rodando: " + e.getMessage());
            }
        }).start();
    }

    public static void abrirNoMicrosoftEdge(String url) {
        new Thread(() -> {
            try {
                // Tenta abrir diretamente com o Microsoft Edge no Windows
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "msedge", url);
                pb.start();
                System.out.println("[Edge] Abrindo Microsoft Edge em: " + url);
            } catch (Exception e) {
                // Fallback para o navegador padrão do sistema
                try {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(url));
                    }
                } catch (Exception ex) {
                    System.err.println("[Edge ERRO] " + ex.getMessage());
                }
            }
        }).start();
    }

    public static void emitirEvento(String tipo, String hobby, int xp, String label) {
        String safeLabel = label != null ? label.replace("\"", "'") : "Jogo Audrey";
        String json = String.format(
            "{\"type\":\"%s\",\"data\":{\"hobby\":\"%s\",\"xpReward\":%d,\"amount\":%d,\"label\":\"%s\"}}",
            tipo, hobby, xp, xp, safeLabel
        );
        String sseData = "data: " + json + "\n\n";

        for (HttpExchange client : sseClients) {
            try {
                OutputStream os = client.getResponseBody();
                os.write(sseData.getBytes("UTF-8"));
                os.flush();
            } catch (Exception e) {
                sseClients.remove(client);
            }
        }
    }

    private static void aplicarCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    // Handlers
    static class IniciarJogoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            aplicarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // O jogo Java traz a janela para a frente do usuário no desktop!
            JogoAudrey.trazerParaFrente();

            String resposta = "{\"success\":true,\"message\":\"Jogo Audrey conectado com sucesso!\",\"isRunning\":true}";
            byte[] bytes = resposta.getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static class StatusJogoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            aplicarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String resposta = "{\"isRunning\":true,\"message\":\"Jogo Audrey em execução\"}";
            byte[] bytes = resposta.getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static class EventoJogoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            aplicarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), "UTF-8");
                System.out.println("[ServidorIntegradoJava Evento] " + body);

                String sseData = "data: " + body + "\n\n";
                for (HttpExchange client : sseClients) {
                    try {
                        OutputStream os = client.getResponseBody();
                        os.write(sseData.getBytes("UTF-8"));
                        os.flush();
                    } catch (Exception e) {
                        sseClients.remove(client);
                    }
                }

                String resp = "{\"success\":true}";
                byte[] bytes = resp.getBytes("UTF-8");
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    static class SseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            aplicarCors(exchange);
            exchange.getResponseHeaders().set("Content-Type", "text/event-stream; charset=utf-8");
            exchange.getResponseHeaders().set("Cache-Control", "no-cache");
            exchange.getResponseHeaders().set("Connection", "keep-alive");
            exchange.sendResponseHeaders(200, 0);

            OutputStream os = exchange.getResponseBody();
            os.write("data: {\"type\":\"CONNECTED\",\"isGameRunning\":true}\n\n".getBytes("UTF-8"));
            os.flush();

            sseClients.add(exchange);
        }
    }

    static class ArquivosEstaticosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            aplicarCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/home.html";

            // Localiza a pasta novoprojeto_tcc ou assets/public
            File file = new File("novoprojeto_tcc" + path);
            if (!file.exists()) {
                file = new File("../novoprojeto_tcc" + path);
            }
            if (!file.exists()) {
                file = new File("novoprojeto_tcc/android/app/src/main/assets/public" + path);
            }
            if (!file.exists()) {
                file = new File("../novoprojeto_tcc/android/app/src/main/assets/public" + path);
            }
            if (!file.exists()) {
                file = new File(path.substring(1));
            }
            if (!file.exists()) {
                file = new File("../" + path.substring(1));
            }

            if (!file.exists() || file.isDirectory()) {
                String erro = "Arquivo não encontrado: " + path;
                exchange.sendResponseHeaders(404, erro.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(erro.getBytes("UTF-8"));
                }
                return;
            }

            String mime = Files.probeContentType(file.toPath());
            if (mime == null) {
                String name = file.getName().toLowerCase();
                if (name.endsWith(".js")) mime = "application/javascript; charset=utf-8";
                else if (name.endsWith(".css")) mime = "text/css; charset=utf-8";
                else if (name.endsWith(".html")) mime = "text/html; charset=utf-8";
                else if (name.endsWith(".png")) mime = "image/png";
                else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) mime = "image/jpeg";
                else if (name.endsWith(".svg")) mime = "image/svg+xml";
                else mime = "application/octet-stream";
            }

            byte[] fileBytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", mime);
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        }
    }
}
