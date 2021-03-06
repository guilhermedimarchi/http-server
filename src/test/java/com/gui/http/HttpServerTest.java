package com.gui.http;

import com.gui.http.handlers.HttpHandler;
import com.gui.http.models.Response;
import org.junit.jupiter.api.Test;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static com.gui.http.util.HttpStatus.OK;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class HttpServerTest {

    @Test
    public void shouldListenToPortAndAcceptConnections() throws IOException, InterruptedException {
        HttpHandler mockHandler = mock(HttpHandler.class);
        when(mockHandler.handle(any())).thenReturn(new Response(OK));
        int port = 8081;
        Thread thread = new Thread(() -> {
            try {
                HttpServer server = new HttpServer(port);
                server.setDefaultHandler(mockHandler);
                server.start();
            } catch (IOException e) {
                fail("could not start server");
            }
        });
        thread.start();

        assertClientCanConnectToPort(port);
        verify(mockHandler, timeout(5000).atLeast(1)).handle(any());
    }

    private void assertClientCanConnectToPort(int port) throws InterruptedException {
        Thread.sleep(2000);
        try (Socket someClient = new Socket("localhost", port)) {
            assertTrue(someClient.isConnected());
            DataOutputStream out = new DataOutputStream(someClient.getOutputStream());
            out.write("HEAD /abc HTTP/1.1".getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail("client socket should be able to connect");
        }
    }

}
