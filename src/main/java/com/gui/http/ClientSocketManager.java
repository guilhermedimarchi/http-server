package com.gui.http;

import java.net.Socket;

public class ClientSocketManager {

    private Socket socket;
    private StaticHandler handler;

    public ClientSocketManager(Socket socket, StaticHandler handler)  {
        this.socket = socket;
        this.handler = handler;
    }

    public void handleClientConnection() throws Exception {
        Request request = new Request(socket.getInputStream());
        Response response = handler.handle(request);

        response.send(socket.getOutputStream());
    }

}
