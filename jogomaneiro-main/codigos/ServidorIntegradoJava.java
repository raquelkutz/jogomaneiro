import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

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

                server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
                server.start();
                iniciado = true;
                System.out.println("[ServidorIntegradoJava] API de integração iniciada na porta " + PORT + "!");
            } catch (Exception e) {
                System.out.println("[ServidorIntegradoJava] Porta " + PORT + " ocupada ou servidor já rodando: " + e.getMessage());
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

}
