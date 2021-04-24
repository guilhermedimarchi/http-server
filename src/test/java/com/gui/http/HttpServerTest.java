package com.gui.http;

import com.gui.http.handlers.HttpHandler;
import com.gui.http.models.Response;
import org.junit.jupiter.api.Test;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static com.gui.http.HttpStatus.OK;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class HttpServerTest {

    @Test
    public void shouldListenToPortAndAcceptConnections() {
        Thread thread = new Thread(() -> {
            try {
                HttpServer server = new HttpServer(8080);
                server.start();
            } catch (IOException e) {
                fail("could not start server");
            }
        });
        thread.start();

        assertClientCanConnectToPort(8080);
    }

    @Test
    public void shouldAllowToSetDefaultHandler() throws IOException {
        HttpHandler mockHandler = mock(HttpHandler.class);
        when(mockHandler.handle(any())).thenReturn(new Response(OK));

        Thread thread = new Thread(() -> {
            try {
                HttpServer server = new HttpServer(8081);
                server.setDefaultHandler(mockHandler);
                server.start();
            } catch (IOException e) {
                fail("could not start server");
            }
        });
        thread.start();

        assertClientCanConnectToPort(8081);
        verify(mockHandler, timeout(3000).times(1)).handle(any());
    }

    private void assertClientCanConnectToPort(int port) {
        try (Socket someClient = new Socket("localhost", port)) {
            assertTrue(someClient.isConnected());
            DataOutputStream out = new DataOutputStream(someClient.getOutputStream());
            out.write("GET / HTTP/1.1".getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            fail("client socket should be able to connect");
        }
    }

}
