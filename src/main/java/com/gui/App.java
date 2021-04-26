package com.gui;

import com.gui.http.HttpServer;
import com.gui.http.handlers.CachedHandler;
import com.gui.http.handlers.StaticHandler;

import java.io.IOException;

public class App {

    public static void main(String[] args) {
        try {
            HttpServer s = new HttpServer(8080);
            s.setDefaultHandler(new CachedHandler(new StaticHandler("www")));
            s.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
