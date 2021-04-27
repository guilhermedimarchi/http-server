package com.gui.http;

import com.gui.http.handlers.CachedFileHandler;
import com.gui.http.handlers.HttpHandler;
import com.gui.http.handlers.FileHandler;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

    private static final Logger LOGGER = Logger.getLogger(HttpServer.class);
    private HttpHandler handler = new CachedFileHandler(new FileHandler("www"));
    private final int port;

    public HttpServer(int port) throws IOException {
        this.port = port;
    }

    public void setDefaultHandler(HttpHandler handler) {
        this.handler = handler;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            LOGGER.info("started server");
            LOGGER.info("listening to port: " + port);
            Socket clientSocket;
            while ((clientSocket = serverSocket.accept()) != null) {
                LOGGER.info("new connection: " + clientSocket.toString());
                ClientSocketManager manager = new ClientSocketManager(clientSocket, handler);
                Thread t = new Thread(manager);
                t.start();
            }
        }
    }

}
