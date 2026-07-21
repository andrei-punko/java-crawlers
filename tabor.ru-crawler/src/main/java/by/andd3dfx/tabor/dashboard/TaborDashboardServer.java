package by.andd3dfx.tabor.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Local dashboard for browsing {@code profiles.json} and {@code photos/}.
 * Hidden / favorite profile ids are persisted in {@code hidden.json} / {@code favorites.json}.
 */
public class TaborDashboardServer {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "html", "text/html; charset=UTF-8",
            "css", "text/css; charset=UTF-8",
            "js", "application/javascript; charset=UTF-8",
            "json", "application/json; charset=UTF-8",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp",
            "gif", "image/gif"
    );

    private final Path profilesPath;
    private final Path photosDir;
    private final Path hiddenPath;
    private final Path favoritesPath;
    private final int port;

    public TaborDashboardServer(Path profilesPath, Path photosDir, Path hiddenPath, Path favoritesPath, int port) {
        this.profilesPath = profilesPath.toAbsolutePath().normalize();
        this.photosDir = photosDir.toAbsolutePath().normalize();
        this.hiddenPath = hiddenPath.toAbsolutePath().normalize();
        this.favoritesPath = favoritesPath.toAbsolutePath().normalize();
        this.port = port;
    }

    public static void main(String[] args) throws IOException {
        Path profiles = Path.of("profiles.json");
        Path photos = Path.of("photos");
        Path hidden = null;
        Path favorites = null;
        int port = 8080;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--profiles".equals(arg) && i + 1 < args.length) {
                profiles = Path.of(args[++i]);
            } else if ("--photos".equals(arg) && i + 1 < args.length) {
                photos = Path.of(args[++i]);
            } else if ("--hidden".equals(arg) && i + 1 < args.length) {
                hidden = Path.of(args[++i]);
            } else if ("--favorites".equals(arg) && i + 1 < args.length) {
                favorites = Path.of(args[++i]);
            } else if ("--port".equals(arg) && i + 1 < args.length) {
                port = Integer.parseInt(args[++i]);
            } else {
                throw new IllegalArgumentException("Unknown or incomplete arg: " + arg);
            }
        }
        Path parent = profiles.toAbsolutePath().getParent();
        if (hidden == null) {
            hidden = parent != null ? parent.resolve("hidden.json") : Path.of("hidden.json");
        }
        if (favorites == null) {
            favorites = parent != null ? parent.resolve("favorites.json") : Path.of("favorites.json");
        }

        new TaborDashboardServer(profiles, photos, hidden, favorites, port).start();
    }

    public void start() throws IOException {
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new IOException(
                    "Cannot bind port " + port + " (is another dashboard already running?). " + e.getMessage(),
                    e);
        }
        server.createContext("/", this::handleRoot);
        server.createContext("/api/profiles", this::handleProfiles);
        server.createContext("/api/hidden", this::handleHidden);
        server.createContext("/api/hide", exchange -> mutateIdList(exchange, hiddenPath, "hiddenIds", true));
        server.createContext("/api/unhide", exchange -> mutateIdList(exchange, hiddenPath, "hiddenIds", false));
        server.createContext("/api/favorites", this::handleFavorites);
        server.createContext("/api/favorite", exchange -> mutateIdList(exchange, favoritesPath, "favoriteIds", true));
        server.createContext("/api/unfavorite", exchange -> mutateIdList(exchange, favoritesPath, "favoriteIds", false));
        server.createContext("/photos", this::handlePhotos);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Tabor dashboard: http://localhost:" + port);
        System.out.println("  profiles:  " + profilesPath);
        System.out.println("  photos:    " + photosDir);
        System.out.println("  hidden:    " + hiddenPath);
        System.out.println("  favorites: " + favoritesPath);
    }

    private void handleRoot(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain; charset=UTF-8", "Method Not Allowed");
            return;
        }
        String path = exchange.getRequestURI().getPath();
        if ("/".equals(path) || path.isBlank()) {
            path = "/index.html";
        }
        String resource = "dashboard" + path;
        try (InputStream in = TaborDashboardServer.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                send(exchange, 404, "text/plain; charset=UTF-8", "Not Found");
                return;
            }
            byte[] body = in.readAllBytes();
            String ext = extension(path);
            sendBytes(exchange, 200, CONTENT_TYPES.getOrDefault(ext, "application/octet-stream"), body);
        }
    }

    private void handleProfiles(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain; charset=UTF-8", "Method Not Allowed");
            return;
        }
        if (!Files.isRegularFile(profilesPath)) {
            send(exchange, 404, "application/json; charset=UTF-8",
                    "{\"error\":\"profiles.json not found\"}");
            return;
        }
        byte[] body = Files.readAllBytes(profilesPath);
        sendBytes(exchange, 200, "application/json; charset=UTF-8", body);
    }

    private void handleHidden(HttpExchange exchange) throws IOException {
        handleIdListGet(exchange, hiddenPath, "hiddenIds");
    }

    private void handleFavorites(HttpExchange exchange) throws IOException {
        handleIdListGet(exchange, favoritesPath, "favoriteIds");
    }

    private void handleIdListGet(HttpExchange exchange, Path file, String field) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain; charset=UTF-8", "Method Not Allowed");
            return;
        }
        ObjectNode root = MAPPER.createObjectNode();
        ArrayNode ids = root.putArray(field);
        for (String id : readIdList(file, field)) {
            ids.add(id);
        }
        send(exchange, 200, "application/json; charset=UTF-8", MAPPER.writeValueAsString(root));
    }

    private void mutateIdList(HttpExchange exchange, Path file, String field, boolean add) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain; charset=UTF-8", "Method Not Allowed");
            return;
        }
        JsonNode body = MAPPER.readTree(exchange.getRequestBody());
        String id = body != null && body.hasNonNull("id") ? body.get("id").asText().trim() : null;
        if (id == null || id.isEmpty()) {
            send(exchange, 400, "application/json; charset=UTF-8", "{\"error\":\"id required\"}");
            return;
        }

        Set<String> ids = readIdList(file, field);
        boolean changed = add ? ids.add(id) : ids.remove(id);
        writeIdList(file, field, ids);

        ObjectNode response = MAPPER.createObjectNode();
        response.put("id", id);
        response.put(add ? "added" : "removed", changed);
        ArrayNode array = response.putArray(field);
        for (String value : ids) {
            array.add(value);
        }
        send(exchange, 200, "application/json; charset=UTF-8", MAPPER.writeValueAsString(response));
    }

    private void handlePhotos(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain; charset=UTF-8", "Method Not Allowed");
            return;
        }
        String path = exchange.getRequestURI().getPath();
        String fileName = path.substring("/photos".length());
        if (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }
        fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        if (fileName.isBlank() || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            send(exchange, 400, "text/plain; charset=UTF-8", "Bad Request");
            return;
        }
        Path file = photosDir.resolve(fileName).normalize();
        if (!file.startsWith(photosDir) || !Files.isRegularFile(file)) {
            send(exchange, 404, "text/plain; charset=UTF-8", "Not Found");
            return;
        }
        String ext = extension(fileName);
        sendBytes(exchange, 200, CONTENT_TYPES.getOrDefault(ext, "application/octet-stream"),
                Files.readAllBytes(file));
    }

    private static Set<String> readIdList(Path file, String field) throws IOException {
        Set<String> ids = new LinkedHashSet<>();
        if (!Files.isRegularFile(file)) {
            return ids;
        }
        JsonNode root = MAPPER.readTree(Files.readAllBytes(file));
        JsonNode array = root != null ? root.get(field) : null;
        if (array != null && array.isArray()) {
            for (JsonNode node : array) {
                if (node != null && !node.asText().isBlank()) {
                    ids.add(node.asText().trim());
                }
            }
        }
        return ids;
    }

    private static void writeIdList(Path file, String field, Set<String> ids) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        ObjectNode root = MAPPER.createObjectNode();
        ArrayNode array = root.putArray(field);
        for (String id : ids) {
            array.add(id);
        }
        Files.writeString(file, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root),
                StandardCharsets.UTF_8);
    }

    private static void send(HttpExchange exchange, int status, String contentType, String body)
            throws IOException {
        sendBytes(exchange, status, contentType, body.getBytes(StandardCharsets.UTF_8));
    }

    private static void sendBytes(HttpExchange exchange, int status, String contentType, byte[] body)
            throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType);
        headers.set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private static String extension(String path) {
        int dot = path.lastIndexOf('.');
        if (dot < 0 || dot == path.length() - 1) {
            return "";
        }
        return path.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
