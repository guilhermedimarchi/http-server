package com.gui.http;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class HttpServerTest {

    @Test
    public void whenPortIsNotGiven_shouldCreateServerUsingDefault8080Port() throws IOException {
        HttpServer server = new HttpServer();
        assertNotNull(server);
        assertThrows(IOException.class, () -> {
             new HttpServer(8080);
        });
        server.close();
    }

    @Test
    public void shouldListenToPort() {
        Thread thread = new Thread(() -> {
            try {
                HttpServer server = new HttpServer(8081);
                server.start();
            } catch (IOException e) {
                fail("could not start server");
            }
        });
        thread.start();
        assertClientCanConnect();
    }

    private void assertClientCanConnect() {
        try(Socket someClient = new Socket("localhost", 8081)) {
            assertTrue(someClient.isConnected());
        } catch (Exception e) {
            fail("client socket should be able to connect");
        }
    }
}
