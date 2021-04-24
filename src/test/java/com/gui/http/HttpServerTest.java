package com.gui.http;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class HttpServerTest {

   @Test
    public void shouldListenToPortAndAcceptConnections() {
        Thread thread = new Thread(() -> {
            try {
                HttpServer server = new HttpServer(8081);
                server.start();
            } catch (IOException e) {
                fail("could not start server");
            }
        });
        thread.start();
        assertClientCanConnectOnPort(8081);
    }

    private void assertClientCanConnectOnPort(int port) {
        try(Socket someClient = new Socket("localhost", port)) {
            assertTrue(someClient.isConnected());
        } catch (Exception e) {
            fail("client socket should be able to connect");
        }
    }
}
