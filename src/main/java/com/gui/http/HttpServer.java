package com.gui.http;

import com.gui.http.handlers.HttpHandler;
import com.gui.http.handlers.StaticHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

    private static int port = 8080;
    private final HttpHandler defaultHandler = new StaticHandler("./");

    public HttpServer() { }

    public HttpServer(int port) throws IOException {
        this.port = port;
    }

    public void start() throws IOException {
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            Socket clientSocket;
            while ((clientSocket = serverSocket.accept()) != null)  {
                ClientSocketManager manager = new ClientSocketManager(clientSocket, defaultHandler);
                Thread t = new Thread(manager);
                t.start();
            }
        }
    }

}
