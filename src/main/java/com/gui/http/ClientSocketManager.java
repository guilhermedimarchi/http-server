package com.gui.http;

import com.gui.http.handlers.HttpHandler;
import com.gui.http.models.Request;
import com.gui.http.models.RequestParseException;
import com.gui.http.models.Response;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;

import static com.gui.http.util.HttpHeader.CONNECTION;
import static com.gui.http.util.HttpHeader.KEEP_ALIVE;
import static com.gui.http.util.HttpStatus.BAD_REQUEST;
import static com.gui.http.util.HttpStatus.INTERNAL_SERVER_ERROR;
import static java.nio.charset.StandardCharsets.*;

public class ClientSocketManager implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientSocketManager.class);
    private final Socket socket;
    private final HttpHandler handler;
    private int maxRequestsPerConnection = 3;

    public ClientSocketManager(Socket socket, HttpHandler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    public void setMaxRequestsPerConnection(int max) {
        this.maxRequestsPerConnection = max;
    }

    @Override
    public void run() {
        try (BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
             BufferedReader input = new BufferedReader(new InputStreamReader(in, US_ASCII))) {
            boolean connected = true;
            int requestCount = 0;
            while (connected) {
                requestCount++;
                Response response;
                try {
                    LOGGER.debug("request received from: " + socket);
                    Request request = new Request(input);
                    response = handler.handle(request);

                    if (clientWantsToCloseConnection(request) || requestCount >= maxRequestsPerConnection) {
                        connected = false;
                        response.addHeader(CONNECTION, "close");
                    } else {
                        response.addHeader(CONNECTION, "keep-alive");
                        response.addHeader(KEEP_ALIVE, "max=" + maxRequestsPerConnection);
                    }

                } catch (RequestParseException e) {
                    LOGGER.error("bad request", e);
                    connected = false;
                    response = new Response(BAD_REQUEST, e.getMessage().getBytes());
                } catch (Exception e) {
                    LOGGER.error("internal server error", e);
                    connected = false;
                    response = new Response(INTERNAL_SERVER_ERROR);
                }
                response.send(out);
            }
        } catch (IOException e) {
            LOGGER.error("error writing response to socket output", e);
        } finally {
            try {
                socket.close();
                LOGGER.info("closing connection: " + socket);
            } catch (IOException e) {
                LOGGER.error("could not close socket", e);
            }
        }
    }

    private boolean clientWantsToCloseConnection(Request request) {
        return request.getHeaders().containsKey(CONNECTION) && "close".equals(request.getHeaders().get(CONNECTION));
    }
}
