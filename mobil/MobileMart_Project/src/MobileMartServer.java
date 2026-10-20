import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;

public class MobileMartServer {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/mobilemart";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456789";


    

    static Connection db() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    static String read(InputStream in) throws IOException {
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    static void send(HttpExchange ex, int code, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type + "; charset=UTF-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }

    static String val(String body, String key) {
        String pattern = "\\\"" + key + "\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(body);
        return m.find() ? m.group(1) : "";
    }

    static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/login", ex -> {
            try {
                if (!ex.getRequestMethod().equalsIgnoreCase("POST")) {
                    send(ex, 405, "application/json", "{\"error\":\"POST required\"}");
                    return;
                }
                String b = read(ex.getRequestBody()), u = val(b, "username"), p = val(b, "password");
                try (Connection c = db();
                        PreparedStatement s = c
                                .prepareStatement("SELECT username FROM admins WHERE username=? AND password=?")) {
                    s.setString(1, u);
                    s.setString(2, p);
                    try (ResultSet r = s.executeQuery()) {
                        send(ex, 200, "application/json", r.next() ? "{\"ok\":true,\"username\":\"" + esc(u) + "\"}"
                                : "{\"ok\":false,\"message\":\"Wrong username or password\"}");
                    }
                }
            }  catch (Exception e) {
    e.printStackTrace();
    try {
        send(ex, 500, "application/json",
            "{\"error\":\"Database connection failed. Check terminal.\"}");
    } catch (IOException ex2) {
        ex2.printStackTrace();
    }
}
        });
        server.createContext("/api/mobiles", ex -> {
            try (Connection c = db();
                    Statement s = c.createStatement();
                    ResultSet r = s.executeQuery("SELECT id,brand,model,price FROM mobiles ORDER BY id")) {
                StringBuilder out = new StringBuilder("[");
                boolean first = true;
                while (r.next()) {
                    if (!first)
                        out.append(',');
                    first = false;
                    out.append("{\"id\":").append(r.getInt("id")).append(",\"brand\":\"")
                            .append(esc(r.getString("brand"))).append("\",\"model\":\"")
                            .append(esc(r.getString("model"))).append("\",\"price\":").append(r.getBigDecimal("price"))
                            .append("}");
                }
                out.append(']');
                send(ex, 200, "application/json", out.toString());
            } catch (Exception e) {
                send(ex, 500, "application/json", "{\"error\":\"Cannot load mobiles. Check database setup.\"}");
            }
        });
        server.createContext("/api/bill", ex -> {
            try {
                String b = read(ex.getRequestBody()), user = val(b, "username");
                java.util.regex.Matcher m = java.util.regex.Pattern
                        .compile("\\{\\s*\\\"id\\\"\\s*:\\s*(\\d+)\\s*,\\s*\\\"quantity\\\"\\s*:\\s*(\\d+)\\s*}")
                        .matcher(b);
                java.util.ArrayList<int[]> items = new java.util.ArrayList<>();
                while (m.find())
                    items.add(new int[] { Integer.parseInt(m.group(1)),
                            Math.max(1, Math.min(99, Integer.parseInt(m.group(2)))) });
                if (items.isEmpty()) {
                    send(ex, 400, "application/json", "{\"error\":\"Cart is empty\"}");
                    return;
                }
                try (Connection c = db()) {
                    c.setAutoCommit(false);
                    try {
                        double total = 0;
                        java.util.ArrayList<Object[]> rows = new java.util.ArrayList<>();
                        for (int[] it : items)
                            try (PreparedStatement s = c
                                    .prepareStatement("SELECT model,price FROM mobiles WHERE id=?")) {
                                s.setInt(1, it[0]);
                                try (ResultSet r = s.executeQuery()) {
                                    if (r.next()) {
                                        double price = r.getDouble("price"), line = price * it[1];
                                        total += line;
                                        rows.add(new Object[] { it[0], r.getString("model"), it[1], price, line });
                                    }
                                }
                            }
                        try (PreparedStatement s = c.prepareStatement("INSERT INTO orders(username,total) VALUES(?,?)",
                                Statement.RETURN_GENERATED_KEYS)) {
                            s.setString(1, user.isBlank() ? "admin" : user);
                            s.setDouble(2, total);
                            s.executeUpdate();
                            try (ResultSet keys = s.getGeneratedKeys()) {
                                keys.next();
                                int order = keys.getInt(1);
                                for (Object[] row : rows)
                                    try (PreparedStatement d = c.prepareStatement(
                                            "INSERT INTO order_items(order_id,mobile_id,model,quantity,unit_price,line_total) VALUES(?,?,?,?,?,?)")) {
                                        d.setInt(1, order);
                                        d.setInt(2, (int) row[0]);
                                        d.setString(3, (String) row[1]);
                                        d.setInt(4, (int) row[2]);
                                        d.setDouble(5, (double) row[3]);
                                        d.setDouble(6, (double) row[4]);
                                        d.executeUpdate();
                                    }
                                c.commit();
                                send(ex, 200, "application/json", "{\"ok\":true,\"orderId\":" + order + ",\"total\":"
                                        + String.format(java.util.Locale.US, "%.2f", total) + "}");
                            }
                        }
                    } catch (Exception e) {
                        c.rollback();
                        throw e;
                    }
                }
            } catch (Exception e) {
                send(ex, 500, "application/json", "{\"error\":\"Bill could not be saved. Check database.\"}");
            }
        });
        server.createContext("/api/feedback", ex -> {
            try {
                String b = read(ex.getRequestBody()), u = val(b, "username"), comments = val(b, "comments");
                int rating = Integer.parseInt(val(b, "rating"));
                if (rating < 1 || rating > 5)
                    throw new Exception();
                try (Connection c = db();
                        PreparedStatement s = c
                                .prepareStatement("INSERT INTO feedback(username,rating,comments) VALUES(?,?,?)")) {
                    s.setString(1, u.isBlank() ? "admin" : u);
                    s.setInt(2, rating);
                    s.setString(3, comments);
                    s.executeUpdate();
                    send(ex, 200, "application/json", "{\"ok\":true}");
                }
            } catch (Exception e) {
                send(ex, 400, "application/json", "{\"error\":\"Please enter a rating from 1 to 5.\"}");
            }
        });
        server.createContext("/", ex -> {
            try {
                String path = ex.getRequestURI().getPath();
                if (path.equals("/"))
                    path = "/index.html";
                Path root = Paths.get("web").toAbsolutePath().normalize(),
                        file = root.resolve(path.substring(1)).normalize();
                if (!file.startsWith(root) || !Files.exists(file) || Files.isDirectory(file)) {
                    send(ex, 404, "text/plain", "Not found");
                    return;
                }
                String type = path.endsWith(".css") ? "text/css"
                        : path.endsWith(".js") ? "application/javascript"
                                : path.endsWith(".svg") ? "image/svg+xml" : "text/html";
                send(ex, 200, type, Files.readString(file));
            } catch (Exception e) {
                send(ex, 500, "text/plain", "Server error");
            }
        });
        server.setExecutor(null);
        server.start();
        System.out.println("MobileMart running at http://localhost:8080");
    }
}
