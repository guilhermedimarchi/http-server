package com.gui;

import com.gui.http.HttpServer;

import java.io.IOException;

public class App {

    public static void main(String[] args) {
        try {
            HttpServer s = new HttpServer(8080);
            s.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
