package com.gui;

import com.gui.http.HttpServer;
import org.apache.log4j.Logger;

import java.io.IOException;

public class App {
    private static final Logger LOGGER = Logger.getLogger(App.class);

    public static void main(String[] args) {
        try {
            HttpServer server = new HttpServer(8080);
            server.start();
        } catch (IOException e) {
            LOGGER.fatal("server could not start", e);
        }
    }

}
