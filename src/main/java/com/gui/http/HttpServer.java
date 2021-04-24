package com.gui.http;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer implements Closeable {

    private static final int defaultPort = 8080;
    private final ServerSocket serverSocket;
    private final HttpHandler defaultHandler = new StaticHandler("./");

    public HttpServer(final int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public HttpServer() throws IOException {
        serverSocket = new ServerSocket(defaultPort);
    }

    public static void main(String[] args) {
        HttpServer s;
        try {
            s = new HttpServer(8080);
            s.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        Socket clientSocket;
        while ((clientSocket = serverSocket.accept()) != null)  {
            ClientSocketManager manager = new ClientSocketManager(clientSocket, defaultHandler);
            manager.handleClientConnection();
        }
    }

    @Override
    public void close() throws IOException {
        this.serverSocket.close();
    }
}
