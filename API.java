import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class API {
    private HttpServer server;

    int port;

    public API(int _port) throws IOException {
        server = HttpServer.create(new java.net.InetSocketAddress(_port), 0);
        server.createContext("/", new RouteHandler());
        server.setExecutor(null);
        port = _port;
    }

    public void start() {
        server.start();
        System.out.println("API started on port " + port);
    }

    public void stop() {
        server.stop(0);
        System.out.println("API stopped");
    }

    static class RouteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            URI uri = exchange.getRequestURI();
            String path = uri.getPath();

            /*
            if (path.startsWith("/download/")) {
            String id = path.substring("/download/".length());

            String response = "Downloading file with ID: " + id;

            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            } else {    
             */
            
            String response = "Minecraft Bedrock Server world backups.\nWritten by Jacob Schnettler";

            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            
            //}
        }
    }
}
