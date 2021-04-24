package com.gui.http;

import java.net.Socket;

import static com.gui.http.HttpStatus.BAD_REQUEST;
import static com.gui.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class ClientSocketManager {

    private final Socket socket;
    private final HttpHandler handler;

    public ClientSocketManager(Socket socket, HttpHandler handler)  {
        this.socket = socket;
        this.handler = handler;
    }

    public void handleClientConnection() throws Exception {
        Response response;
        try {
            Request request = new Request(socket.getInputStream());
            response = handler.handle(request);
        } catch (RequestParseException e) {
            response = new Response(BAD_REQUEST);
        } catch (Exception e) {
            response = new Response(INTERNAL_SERVER_ERROR);
        }
        response.send(socket.getOutputStream());
    }

}
