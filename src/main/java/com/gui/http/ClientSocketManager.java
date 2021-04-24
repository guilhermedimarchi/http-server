package com.gui.http;

import com.gui.http.handlers.HttpHandler;
import com.gui.http.models.Request;
import com.gui.http.models.RequestParseException;
import com.gui.http.models.Response;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.Socket;

import static com.gui.http.HttpStatus.BAD_REQUEST;
import static com.gui.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class ClientSocketManager implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientSocketManager.class);
    private final Socket socket;
    private final HttpHandler handler;

    public ClientSocketManager(Socket socket, HttpHandler handler)  {
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
        Response response;
        try {
            Request request = new Request(socket.getInputStream());
            response = handler.handle(request);
        } catch (RequestParseException e) {
            LOGGER.error("bad request", e);
            response = new Response(BAD_REQUEST, e.getMessage().getBytes());
        } catch (Exception e) {
            LOGGER.error("internal server error", e);
            response = new Response(INTERNAL_SERVER_ERROR);
        }

        try {
            response.send(socket.getOutputStream());
        } catch (IOException e) {
            LOGGER.error("error writing response to socket output", e);
        }
    }

}
