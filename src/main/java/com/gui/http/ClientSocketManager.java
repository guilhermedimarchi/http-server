package com.gui.http;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.Socket;

import static com.gui.http.HttpStatus.BAD_REQUEST;
import static com.gui.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class ClientSocketManager {

    static final Logger LOGGER = Logger.getLogger(ClientSocketManager.class);
    private final Socket socket;
    private final HttpHandler handler;

    public ClientSocketManager(Socket socket, HttpHandler handler)  {
        this.socket = socket;
        this.handler = handler;
    }

    public void handleClientConnection() {
        Response response;
        try {
            Request request = new Request(socket.getInputStream());
            response = handler.handle(request);
        } catch (RequestParseException e) {
            response = new Response(BAD_REQUEST);
        } catch (Exception e) {
            LOGGER.error("Internal server error", e);
            response = new Response(INTERNAL_SERVER_ERROR);
        }

        try {
            response.send(socket.getOutputStream());
        } catch (IOException e) {
            LOGGER.error("error writing response to socket output", e);
        }
    }

}
