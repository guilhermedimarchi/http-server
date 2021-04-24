package com.gui.http;

import com.gui.http.handlers.HttpHandler;
import com.gui.http.handlers.StaticHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

    private static final int defaultPort = 8080;
    private final ServerSocket serverSocket;
    private final HttpHandler defaultHandler = new StaticHandler("./");

    public HttpServer() throws IOException {
        serverSocket = new ServerSocket(defaultPort);
    }

    public HttpServer(final int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() throws IOException {
        Socket clientSocket;
        while ((clientSocket = serverSocket.accept()) != null)  {
            ClientSocketManager manager = new ClientSocketManager(clientSocket, defaultHandler);
            manager.handleClientConnection();
        }
    }

    public void close() throws IOException {
        this.serverSocket.close();
    }
}
